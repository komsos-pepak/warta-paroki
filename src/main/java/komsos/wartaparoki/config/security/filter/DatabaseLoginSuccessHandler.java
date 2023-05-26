package komsos.wartaparoki.config.security.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import komsos.wartaparoki.feature.pengguna.model.PenggunaAuth;
import komsos.wartaparoki.feature.token.Token;
import komsos.wartaparoki.feature.token.TokenProvider;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.utils.CookieUtil;
import komsos.wartaparoki.utils.SecurityCipher;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class DatabaseLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Value("${application.security-cipher-key}")
    private final String securityCipherKey;

    @Value("${application.cookie-domain}")
	private final String cookieDomain;

	private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		ResponseDto<Map<String, String>> responseDto = new ResponseDto<>();
        PenggunaAuth user = (PenggunaAuth) authentication.getPrincipal();
        Token accessToken = tokenProvider.generateAccessToken(user.getUser(), request);
        Token refreshToken = tokenProvider.generateRefreshToken(user.getUser(), request);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken.getTokenValue());
        tokens.put("refreshToken", SecurityCipher.encrypt(refreshToken.getTokenValue(), securityCipherKey));
        responseDto.setStatus(true);
        responseDto.setPayload(tokens);
        responseDto.setMessage("Authentication Success");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(accessToken.getTokenValue(), accessToken.getDuration()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(refreshToken.getTokenValue(), refreshToken.getDuration()).toString());
        new ObjectMapper().writeValue(response.getOutputStream(), responseDto);
	}

}
