package komsos.wartaparoki.feature.pengguna.model;

import java.util.UUID;

import lombok.Data;

@Data
public class PenggunaResponse {
    private UUID publicId;
    private String nama;
    private String username;
    private Boolean isLocked;
}
