package komsos.wartaparoki.feature.hakAkses.model;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HakAksesRequest {
    @NotBlank(message = "Data nama tidak boleh kosong")
    private String nama;
    @NotNull(message = "Data status aktif tidak boleh kosong")
    private Boolean isActive;
    @NotBlank(message = "Data kode tidak boleh kosong")
    private String kode;
    private String deskripsi;
    @NotNull(message = "Data modul tidak boleh kosong")
    private UUID modulPublicId;
}
