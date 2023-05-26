package komsos.wartaparoki.helper;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import komsos.wartaparoki.feature.pengguna.PenggunaService;
import komsos.wartaparoki.feature.pengguna.model.DetailPenggunaResponse;

@Component
@RequiredArgsConstructor
public class UserPrincipal {
    private final PenggunaService penggunaService;

    public DetailPenggunaResponse getUserPrincipal() {
        UUID publicId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
        DetailPenggunaResponse userOpt = penggunaService.findByPublicIdPrincipal(publicId);
        return userOpt;
    }
}
