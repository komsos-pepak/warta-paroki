package komsos.wartaparoki.feature.modul.model;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ModulHakAksesRequest {
    @NotNull(message = "Atribut nama tidak boleh kosong")
    private String nama;
    @NotNull(message = "Atribut kode tidak boleh kosong")
    private String kode;
    private String deskripsi;
}
