package komsos.wartaparoki.feature.project.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank(message = "Data kode tidak boleh kosong")
    private String kode;
    @NotBlank(message = "Data nama tidak boleh kosong")
    private String nama;
    private String deskripsi;
    @NotNull(message = "Data status aktif tidak boleh kosong")
    private Boolean isActive;
}
