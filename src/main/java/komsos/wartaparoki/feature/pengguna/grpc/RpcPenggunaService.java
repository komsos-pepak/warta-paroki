package komsos.wartaparoki.feature.pengguna.grpc;

import java.util.UUID;

import komsos.wartaparoki.feature.pengguna.PenggunaService;
import komsos.wartaparoki.feature.pengguna.grpc.converter.PenggunaMapper;
import komsos.wartaparoki.feature.pengguna.model.DetailPenggunaResponse;

import clobasoft.erp.grpc.PenggunaRpcRequest;
import clobasoft.erp.grpc.PenggunaRpcResponse;
import clobasoft.erp.grpc.PenggunaRpcServiceGrpc.PenggunaRpcServiceImplBase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RpcPenggunaService extends PenggunaRpcServiceImplBase{

    private final PenggunaService penggunaService;
    private final PenggunaMapper penggunaMapper;
    
    @Override
    public void getPenggunaByPublicIdRpc(PenggunaRpcRequest request, StreamObserver<PenggunaRpcResponse> responseObserver) {
        log.info("getPenggunaByPublicIdRpc {}", request);
        DetailPenggunaResponse userOpt = penggunaService.findByPublicIdPrincipal(UUID.fromString(request.getPublicId()));
        responseObserver.onNext(penggunaMapper.detailPenggunaResponseToPenggunaRpcResponse(userOpt));
        responseObserver.onCompleted();
    }
    
}
