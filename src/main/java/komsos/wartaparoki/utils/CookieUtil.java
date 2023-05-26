package komsos.wartaparoki.utils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.Serializable;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import komsos.wartaparoki.exception.CustomUnauthorizedException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author: TCMALTUNKAN - MEHMET ANIL ALTUNKAN
 * @Date: 22.11.2019:15:10, Cum
 **/
@Component
public class CookieUtil {

    @Value("${application.refresh-token-cookie-name}")
    private String refreshTokenCookieName;

    @Value("${application.access-token-cookie-name}")
    private String accessTokenCookieName;

    @Value("${application.cookie-secure}")
    private Boolean cookieSecure;

    @Value("${application.security-cipher-key}")
    private String securityCipherKey;

    @Value("${application.cookie-same-site}")
    private String cookieSameSite;

    @Value("${application.cookie-domain}")
    private String cookieDomain;

    public HttpCookie createRefreshTokenCookie(String token, Long duration) {
        String encryptedToken = SecurityCipher.encrypt(token, securityCipherKey);
        return ResponseCookie.from(refreshTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .secure(cookieSecure)
                .domain(cookieDomain)
                .sameSite(cookieSameSite)
                .path("/")
                .build();
    }

    public HttpCookie createAccessTokenCookie(String token, Long duration) {
        String encryptedToken = SecurityCipher.encrypt(token, securityCipherKey);
        return ResponseCookie.from(accessTokenCookieName, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .secure(cookieSecure)
                .domain(cookieDomain)
                .sameSite(cookieSameSite)
                .path("/")
                .build();
    }

    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(accessTokenCookieName, "")
                .maxAge(0L)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .build();
    }

    public HttpCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0L)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .build();
    }

    public String getJwtFromCookie(HttpServletRequest request) {
        Optional<String> authorizationHeader = Optional.ofNullable(request.getHeader(AUTHORIZATION));
        if (authorizationHeader.isPresent()) {
            String refreshToken = authorizationHeader.get().substring("Bearer ".length());
            if (refreshToken != null) {
                return refreshToken;
            } else {
                throw new CustomUnauthorizedException("Unauthorized", "Tidak memiliki akses data");
            }
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (accessTokenCookieName.equals(cookie.getName())) {
                        String accessToken = cookie.getValue();
                        if (accessToken != null) {
                            return SecurityCipher.decrypt(accessToken, securityCipherKey);
                        } else {
                            throw new CustomUnauthorizedException("Unauthorized", "Tidak memiliki akses data");
                        }
                    }
                }
            } else {
                throw new CustomUnauthorizedException("Unauthorized", "Tidak memiliki akses data");
            }
        }
        throw new CustomUnauthorizedException("Unauthorized", "Tidak memiliki akses data");
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())));
    }

    public static String serialize(Serializable object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }
}