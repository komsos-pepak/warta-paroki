package komsos.wartaparoki.config.security.filter;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import komsos.wartaparoki.exception.CustomUnauthorizedException;
import komsos.wartaparoki.exception.UserIsLockedException;
import komsos.wartaparoki.feature.pengguna.Pengguna;
import komsos.wartaparoki.feature.pengguna.PenggunaService;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.utils.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final PenggunaService penggunaService;
    private final CookieUtil cookieUtil;

    @Value("${application.token-secret}")
    private final String tokenSecret;

    @Value("${application.access-token-cookie-name}")
    private String accessTokenCookieName;

    @Value("${server.servlet.context-path}")
    private String prefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getServletPath().equals("/v1/login")
                || request.getServletPath().equals("/v1/token/refresh")
                || request.getServletPath().equals("/v1/logout")) {
            filterChain.doFilter(request, response);
        } else {
            try {
                String token = cookieUtil.getJwtFromCookie(request);
                Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                UUID publicId = UUID.fromString(decodedJWT.getClaim("publicId").asString());
                // String Penggunaname = decodedJWT.getClaim("Penggunanmae").asString();
                if (decodedJWT.getSubject() != null) {
                    response.setHeader("error", "Token Invalid");
                    response.setStatus(UNAUTHORIZED.value());
                    Map<String, Object> error = new HashMap<>();
                    List<String> errorMessage = new ArrayList<>();
                    errorMessage.add("Token Invalid");
                    error.put("status", false);
                    error.put("payload", null);
                    error.put("message", "invalid");
                    error.put("errorMessage", errorMessage);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                }
                String[] permissions = decodedJWT.getClaim("hakAkses").asArray(String.class);
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Stream<String> stream = Arrays.stream(permissions);
                stream.forEach(permission -> {
                    authorities.add(new SimpleGrantedAuthority(permission));
                });
                Optional<Pengguna> PenggunaDto = penggunaService.findByPublicId(publicId);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        PenggunaDto.get(), publicId, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch (TokenExpiredException tee) {
                response.setHeader("error", tee.getMessage());
                ResponseDto<Boolean> error = new ResponseDto<>();
                List<String> errorMessage = new ArrayList<>();
                errorMessage.add(tee.getMessage());
                error.setErrorMessage(errorMessage);
                error.setStatus(false);
                error.setPayload(false);
                error.setMessage("Token Expired");
                response.setStatus(UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            } catch (UserIsLockedException uile) {
                response.setHeader("error", uile.getMessage());
                ResponseDto<Boolean> error = new ResponseDto<>();
                List<String> errorMessage = new ArrayList<>();
                errorMessage.add(uile.getCustomMessage());
                error.setErrorMessage(errorMessage);
                error.setStatus(false);
                error.setPayload(false);
                error.setMessage(uile.getMessage());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            } catch (CustomUnauthorizedException cue) {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                response.setHeader("error", e.getMessage());
                ResponseDto<Boolean> error = new ResponseDto<>();
                List<String> errorMessage = new ArrayList<>();
                errorMessage.add(e.getMessage());
                error.setErrorMessage(errorMessage);
                error.setStatus(false);
                error.setPayload(false);
                error.setMessage("Failed to save data");
                if (e.getMessage().contains("expired")) {
                    error.setMessage("Token Expired");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                } else if (e.getMessage().contains("NoSuchElementException")
                        || e.getMessage().contains("NotFoundException")) {
                    error.setMessage(e.getLocalizedMessage());
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                } else if (e.getMessage().contains("RuntimeException")) {
                    error.setMessage(e.getLocalizedMessage());
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                } else {
                    error.setMessage("Kesalahan Server");
                    error.setErrorMessage(Arrays.asList("Ooops! Terjadi kesalahan pada server"));
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }
    }
}