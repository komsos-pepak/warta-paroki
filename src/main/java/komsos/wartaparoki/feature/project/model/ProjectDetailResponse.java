package komsos.wartaparoki.feature.project.model;

import java.util.List;
import java.util.UUID;

import komsos.wartaparoki.feature.modul.model.ModulDetailResponse;
import lombok.Data;

@Data
public class ProjectDetailResponse {
    private UUID publicId;
    private Boolean isActive;
    private String kode;
    private String nama;
    private String deskripsi;
    private List<ModulDetailResponse> modul;
}
