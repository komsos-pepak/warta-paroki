package komsos.wartaparoki.feature.modul.model;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModulRequest {
    @NotEmpty(message = "Data nama tidak boleh kosong")
    private String nama;
    @NotEmpty(message = "Data kode tidak boleh kosong")
    private String kode;
    private String deskripsi;
    @NotNull(message = "Data project tidak boleh kosong")
    private UUID projectPublicId;
    @NotNull(message = "Data status aktif tidak boleh kosong")
    private Boolean isActive;
    List<ModulHakAksesRequest> hakAkses;
}
