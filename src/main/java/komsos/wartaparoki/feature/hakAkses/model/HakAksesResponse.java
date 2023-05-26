package komsos.wartaparoki.feature.hakAkses.model;

import java.util.UUID;

import lombok.Data;

@Data
public class HakAksesResponse {
    private UUID publicId;
    private Boolean isActive;
    private String nama;
    private String kode;
    private String deskripsi;
}
