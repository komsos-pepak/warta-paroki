package komsos.wartaparoki.feature.token;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import komsos.wartaparoki.feature.hakAkses.HakAkses;
import komsos.wartaparoki.feature.pengguna.Pengguna;
import komsos.wartaparoki.feature.pengguna.PenggunaRepository;
import komsos.wartaparoki.feature.pengguna.interfaceClass.ModuleKodeInterface;
import komsos.wartaparoki.feature.peran.Peran;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${application.token-secret}")
    private String tokenSecret;

    @Value("${application.access-token-expiration-milisec}")
    private Long accessTokenExpirationMsec;

    @Value("${application.refresh-token-expiration-milisec}")
    private Long refreshTokenExpirationMsec;

    private final PenggunaRepository penggunaRepository;

    public Token generateAccessToken(Pengguna user, HttpServletRequest request) {
        List<ModuleKodeInterface> modul = penggunaRepository.getModuls(user.getId());
        List<String> projectList = new ArrayList<>();
        for (ModuleKodeInterface moduleKodeInterface : modul) {
            if (moduleKodeInterface != null) {
                projectList.add(moduleKodeInterface.getProjectKode());
            }
        }
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
        Long duration = accessTokenExpirationMsec/1000;
        Date now = new Date();
        Long expiredLong = now.getTime() + accessTokenExpirationMsec;
        Date expiryDate = new Date(expiredLong);
        String accessToken = JWT.create()
        .withExpiresAt(expiryDate)  //3 menit
        .withIssuer(request.getRequestURI().toString())
        .withClaim("hakAkses", getAuthorities(user.getPeran()).stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .withClaim("project", projectList)
        .withClaim("nama", user.getNama())
        .withClaim("username", user.getUsername())
        .withClaim("publicId", user.getPublicId().toString())
        .sign(algorithm);
        return new Token(Token.TokenType.ACCESS, accessToken, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    public Token generateRefreshToken(Pengguna user, HttpServletRequest request) {
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
        Long duration = refreshTokenExpirationMsec/1000;
        Date now = new Date();
        Long expiredLong = now.getTime() + refreshTokenExpirationMsec;
        Date expiryDate = new Date(expiredLong);
        String refreshToken = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(expiryDate)  //1 hari
        .withIssuer(request.getRequestURI().toString())
        .withClaim("username", user.getUsername())
        .withClaim("publicId", user.getPublicId().toString())
        .sign(algorithm);
        return new Token(Token.TokenType.REFRESH, refreshToken, duration, LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Peran> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Peran> roles) {
        List<String> hakAksesList = new ArrayList<>();
        for (Peran role : roles) {
            for (HakAkses hakAkses : role.getHakAkses()) {
                if (!hakAksesList.contains(hakAkses.getKode())) {
                    hakAksesList.add(hakAkses.getKode());
                }
            }
        }
        return hakAksesList;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : permissions) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
}
