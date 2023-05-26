package komsos.wartaparoki.feature.modul.model;

import java.util.List;
import java.util.UUID;

import komsos.wartaparoki.feature.hakAkses.model.HakAksesResponse;
import lombok.Data;

@Data
public class ModulDetailResponse {
    private UUID publicId;
    private String nama;
    private String kode;
    private Boolean isActive;
    private List<HakAksesResponse> hakAkses;
}
