package komsos.wartaparoki.config.security.oauth;

import static komsos.wartaparoki.config.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import komsos.wartaparoki.exception.CustomUnauthorizedException;
import komsos.wartaparoki.feature.pengguna.Pengguna;
import komsos.wartaparoki.feature.pengguna.PenggunaRepository;
import komsos.wartaparoki.feature.pengguna.PenggunaService;
import komsos.wartaparoki.feature.token.Token;
import komsos.wartaparoki.feature.token.TokenProvider;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.utils.CookieUtil;
import komsos.wartaparoki.utils.SecurityCipher;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class OAuthLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	
    private final String securityCipherKey;
    
	private final String authorizedRedirectUris;
	
	private final PenggunaService userService;
	private final PenggunaRepository userRepository;
	private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
		String oauth2ClientName = oauth2User.getOauth2ClientName();
		String username = oauth2User.getEmail();
		Optional<Pengguna> userOpt = userRepository.findByUsername(username);
		Pengguna pengguna = new Pengguna();
		if (userOpt.isPresent()) {
			pengguna = userService.updateAuthenticationType(username, oauth2ClientName);
		} else {
			pengguna = userService.createUserByGoogleOauth(oauth2User);
		}
		ResponseDto<Map<String, String>> responseDto = new ResponseDto<>();
        Token accessToken = tokenProvider.generateAccessToken(pengguna, request);
        Token refreshToken = tokenProvider.generateRefreshToken(pengguna, request);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken.getTokenValue());
        tokens.put("refreshToken", SecurityCipher.encrypt(refreshToken.getTokenValue(), securityCipherKey));
        responseDto.setStatus(true);
        responseDto.setPayload(tokens);
        responseDto.setMessage("Authentication Success");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(accessToken.getTokenValue(), accessToken.getDuration()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(refreshToken.getTokenValue(), refreshToken.getDuration()).toString());
		String targetUrl = determineTargetUrl(request, response, authentication, accessToken);
		response.sendRedirect(targetUrl);
        new ObjectMapper().writeValue(response.getOutputStream(), responseDto);
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication,  Token accessToken) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new CustomUnauthorizedException("Maaf! Kami memiliki URI Pengalihan Tidak Sah dan tidak dapat melanjutkan autentikasi", "Unauthorized");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String token = accessToken.getTokenValue();

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
    }
    
    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        String[] allowedCorsString = authorizedRedirectUris.split(",");
        return Arrays.asList(allowedCorsString)
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use different paths if they want to
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    if(authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost()) && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                        return true;
                    }
                    return false;
                });
    }


}
