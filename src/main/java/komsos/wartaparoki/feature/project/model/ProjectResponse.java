package komsos.wartaparoki.feature.project.model;

import java.util.UUID;

import lombok.Data;

@Data
public class ProjectResponse {
    private UUID publicId;
    private String kode;
    private Boolean isActive;
    private String nama;
    private String deskripsi;
}
