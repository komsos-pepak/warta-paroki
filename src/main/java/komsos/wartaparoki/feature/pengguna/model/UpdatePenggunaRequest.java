package komsos.wartaparoki.feature.pengguna.model;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePenggunaRequest {
    @NotBlank(message = "Atribut nama tidak boleh kosong")
    private String nama;
    @NotBlank(message = "Atribut username tidak boleh kosong")
    private String username;
    @NotEmpty(message = "Atribut peran pengguna tidak boleh kosong")
    private List<UUID> listPeranPublicId;
    @NotNull(message = "Atribut isLocked tidak boleh kosong")
    private Boolean isLocked;
}
