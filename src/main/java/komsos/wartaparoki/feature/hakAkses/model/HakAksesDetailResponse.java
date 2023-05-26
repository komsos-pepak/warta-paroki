package komsos.wartaparoki.feature.hakAkses.model;

import lombok.Data;

import java.util.UUID;

import komsos.wartaparoki.feature.modul.model.ModulResponse;

@Data
public class HakAksesDetailResponse {
    private UUID publicId;
    private Boolean isActive;
    private String nama;
    private String kode;
    private String deskripsi;
    private ModulResponse modul;
}
