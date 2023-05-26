package komsos.wartaparoki.feature.pengguna.grpc.converter;

import org.springframework.stereotype.Component;

import komsos.wartaparoki.feature.pengguna.model.DetailPenggunaResponse;

import clobasoft.erp.grpc.PenggunaRpcResponse;

@Component
public class PenggunaMapper {
    public PenggunaRpcResponse detailPenggunaResponseToPenggunaRpcResponse(DetailPenggunaResponse detailPenggunaResponse) {
        return PenggunaRpcResponse.newBuilder()
                .setNama(detailPenggunaResponse.getNama())
                .setUsername(detailPenggunaResponse.getUsername())
                .setPasswordKedaluwarsa(detailPenggunaResponse.getPasswordKedaluwarsa().toString())
                .setPublicId(detailPenggunaResponse.getPublicId().toString())
                .addAllHakAkses(detailPenggunaResponse.getHakAkses())
                .addAllWewenang(detailPenggunaResponse.getWewenang())
                .build();
    }
}
