package komsos.wartaparoki.feature.modul.model;

import java.util.UUID;

import komsos.wartaparoki.feature.project.Project;
import lombok.Data;

@Data
public class ModulResponse {
    private UUID publicId;
    private String nama;
    private String kode;
    private String projectName;
    private Boolean isActive;

    public void setProject(Project project) {
        this.projectName = project.getNama();
    }
}
