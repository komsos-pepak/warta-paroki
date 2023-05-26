package komsos.wartaparoki.feature.token;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import komsos.wartaparoki.feature.pengguna.Pengguna;
import komsos.wartaparoki.feature.pengguna.PenggunaService;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.utils.CookieUtil;
import komsos.wartaparoki.utils.SecurityCipher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/token")
@RequiredArgsConstructor
@Slf4j
public class TokenController {
    
    @Value("${application.refresh-token-cookie-name}")
    private String refreshTokenCookieName;

    @Value("${application.token-secret}")
    private String tokenSecret;

    @Value("${application.security-cipher-key}")
    private String securityCipherKey;

    @Value("${application.cookie-domain}")
    private String cookieDomain;
    
    private final TokenProvider tokenProvider;
    
    private final PenggunaService penggunaService;
    
    private final CookieUtil cookieUtil;

    @GetMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, IllegalArgumentException {
        ResponseDto<Map<String, String>> responseDto = new ResponseDto<>();
        log.info("REFRESH-TOKEN");
        try {
            String refreshTokenString = getJwtFromCookie(request);
            Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = JWT.decode(refreshTokenString);
            if( jwt.getExpiresAt().before(new Date())) {
                response.setStatus(UNAUTHORIZED.value());
                Map<String, Object> error = new HashMap<>();
                List<String> errorMessage = new ArrayList<>();
                errorMessage.add("Token sudah kadaluarsa");
                error.put("errorMessage", errorMessage);
                error.put("status", false);
                error.put("payload", null);
                error.put("message", "Token kadaluarsa");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
            DecodedJWT decodedJWT = verifier.verify(refreshTokenString);
            String username = decodedJWT.getClaim("username").asString();
            if (username == null) {
                log.error("Error Logging in: {}", "Token Invalid");
                response.setHeader("error", "Token Invalid");
                response.setStatus(UNAUTHORIZED.value());
                Map<String, String> error = new HashMap<>();
                error.put("error_message", "Token Invalid");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
            UUID publicId = UUID.fromString(decodedJWT.getClaim("publicId").asString());
            Optional<Pengguna> userOpt = penggunaService.findByPublicId(publicId);
            if (!userOpt.get().getUsername().equals(username)) {
                log.error("Error Logging in: {}", "Token Invalid");
                response.setHeader("error", "Token Invalid");
                response.setStatus(UNAUTHORIZED.value());
                Map<String, Object> error = new HashMap<>();
                List<String> errorMessage = new ArrayList<>();
                errorMessage.add("Token Invalid");
                error.put("status", false);
                error.put("message", "invalid");
                error.put("errorMessage", errorMessage);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
            Token accessToken = tokenProvider.generateAccessToken(userOpt.get(), request);
            Token refreshToken = tokenProvider.generateRefreshToken(userOpt.get(), request);
            refreshTokenString = refreshToken.getTokenValue();
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
        } catch (Exception e) {
            response.setStatus(UNAUTHORIZED.value());
            Map<String, Object> error = new HashMap<>();
            List<String> errorMessage = new ArrayList<>();
            errorMessage.add(e.getLocalizedMessage());
            error.put("errorMessage", errorMessage);
            error.put("status", false);
            error.put("message", "UNAUTHORIZED");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), error);
        }
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    String accessToken = cookie.getValue();
                    if (accessToken != null) {
                        return SecurityCipher.decrypt(accessToken, securityCipherKey);
                    }
                }
            }
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            String refreshToken = authorizationHeader.substring("Bearer ".length());
            if (refreshToken != null) {
                return SecurityCipher.decrypt(refreshToken, securityCipherKey);
            } else {
                return null;
            }
        }
        return null;
    }
}
