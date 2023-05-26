package komsos.wartaparoki.feature.peran.model;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PeranRequest {
    @NotBlank(message = "Data nama tidak boleh kosong")
    private String nama;
    @NotBlank(message = "Data kode tidak boleh kosong")
    private String kode;
    private String deskripsi;

    @NotNull(message = "Data status aktif tidak boleh kosong")
    private Boolean isActive;

    @NotEmpty(message = "Data Hak Akses tidak boleh kosong")
    private List<UUID> hakAksesPublicId;
}
