package komsos.wartaparoki.feature.pengguna.model;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PenggunaRequest {
    @NotBlank(message = "Atribut nama tidak boleh kosong")
    private String nama;
    @NotBlank(message = "Atribut username tidak boleh kosong")
    private String username;
    @NotBlank(message = "Atribut password tidak boleh kosong")
    private String password;
    @NotEmpty(message = "Atribut peran pengguna tidak boleh kosong")
    private List<UUID> listPeranPublicId;
}
