package komsos.wartaparoki.feature.peran.model;

import java.util.List;
import java.util.UUID;

import komsos.wartaparoki.feature.hakAkses.model.HakAksesResponse;
import lombok.Data;

@Data
public class PeranDetailResponse {
    private UUID publicId;
    private Boolean isActive;
    private String nama;
    private String kode;
    private String deskripsi;
    private List<HakAksesResponse> hakAkses;
}
