package komsos.wartaparoki.feature.peran.model;

import java.util.UUID;

import lombok.Data;

@Data
public class PeranResponse {
    private UUID publicId;
    private Boolean isActive;
    private String nama;
    private String kode;
    private String deskripsi;
}
