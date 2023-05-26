package komsos.wartaparoki.feature.peran;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import komsos.wartaparoki.exception.DuplicateResourceException;
import komsos.wartaparoki.exception.ResourceNotFoundException;
import komsos.wartaparoki.feature.hakAkses.HakAkses;
import komsos.wartaparoki.feature.hakAkses.HakAksesRepo;
import komsos.wartaparoki.feature.peran.model.PeranDetailResponse;
import komsos.wartaparoki.feature.peran.model.PeranRequest;
import komsos.wartaparoki.feature.peran.model.PeranResponse;
import komsos.wartaparoki.helper.CustomPage;
import komsos.wartaparoki.helper.GenericOrSpesification;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.helper.SessionFilter;
import komsos.wartaparoki.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PeranService {
    private final PeranRepo peranRepo;
    private final HakAksesRepo hakAksesRepo;
    private final ModelMapper modelMapper;
    private final SessionFilter sessionFilter;
    private final Utils utils;

    public ResponseEntity<ResponseDto<Boolean>> tambahPeran(PeranRequest peranRequest) {
        log.info("tambahPeran");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Peran> peranOpt = peranRepo.findByKode(peranRequest.getKode());
        sessionFilter.closeSessionFilterIsDeleted();
        if (peranOpt.isPresent()) {
            throw new DuplicateResourceException("Nama Peran " +peranRequest.getKode()+ " sudah digunakan", "Gagal menambahkan Data");
        }else {
            Set<HakAkses> hakAksesList = new HashSet<>();
            for (UUID hakAksesId : peranRequest.getHakAksesPublicId()) {
                Optional<HakAkses> hakAksesOpt = hakAksesRepo.findByPublicId(hakAksesId);
                if (hakAksesOpt.isPresent()) {
                    hakAksesList.add(hakAksesOpt.get());
                } else {
                    throw new ResourceNotFoundException("Data Hak Akses tidak ditemukan", "Data tidak ditemukan di basis data");
                }
            }
            Peran peran = modelMapper.map(peranRequest, Peran.class);
            peran.setPublicId(UUID.randomUUID());
            peran.setHakAkses(hakAksesList);
            peranRepo.save(peran);
            responseDto.setStatus(true);
            responseDto.setMessage("Berhasil menyimpan data");
            responseDto.setPayload(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        }
    }

    public ResponseEntity<ResponseDto<CustomPage<PeranResponse>>> searchPeran(Pageable paging, GenericOrSpesification<Peran> genericSpesification) {
        ResponseDto<CustomPage<PeranResponse>> responseDto = new ResponseDto<>();
        Page<Peran> peranPage = peranRepo.findAll(genericSpesification, paging);
        if (peranPage.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<PeranResponse> peranResponsePage = utils.mapEntityPageIntoDtoPage(peranPage, PeranResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Peran ditemukan");
        responseDto.setPayload(peranResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<CustomPage<PeranResponse>>> getPeranPageable(Pageable paging) {
        ResponseDto<CustomPage<PeranResponse>> responseDto = new ResponseDto<>();
        Page<Peran> peran = peranRepo.findAll(paging);
        if (peran.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<PeranResponse> peranResponsePage = utils.mapEntityPageIntoDtoPage(peran, PeranResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Peran ditemukan");
        responseDto.setPayload(peranResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<Boolean>> hapusPeranByPublicId(UUID publicId) {
        log.info("hapusPeranById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Optional<Peran> peranOpt = peranRepo.findByPublicId(publicId);
        if (peranOpt.isEmpty()) {
            throw new ResourceNotFoundException("Peran dengan ID : " +publicId+ " tidak ditemukan", "Gagal menghapus data");
        }else{
            peranRepo.delete(peranOpt.get());
            responseDto.setStatus(true);
            responseDto.setMessage("Data berhasil dihapus");
            responseDto.setPayload(true);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseEntity<ResponseDto<Boolean>> ubahPeranByPublicId(UUID publicId, PeranRequest peranRequest) {
        log.info("ubahPeranById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Peran> peranOpt = peranRepo.findByPublicId(publicId);
        sessionFilter.closeSessionFilterIsDeleted();
        if (peranOpt.isPresent()) {
            sessionFilter.openSessionFilterIsDeleted(false);
            Optional<Peran> checkKodePeranOpt = peranRepo.findByKode(peranRequest.getKode());
            if (checkKodePeranOpt.isPresent() && !checkKodePeranOpt.get().getPublicId().equals(publicId)) {
                throw new DuplicateResourceException("Peran dengan kode : " +peranRequest.getKode()+ " sudah digunakan", "Gagal mengubah Data");
            } else {
                Set<HakAkses> hakAksesList = new HashSet<>();
                for (UUID hakAksesId : peranRequest.getHakAksesPublicId()) {
                    Optional<HakAkses> hakAksesOpt = hakAksesRepo.findByPublicId(hakAksesId);
                    if (hakAksesOpt.isPresent()) {
                        hakAksesList.add(hakAksesOpt.get());
                    } else {
                        throw new ResourceNotFoundException("Data Hak Akses tidak ditemukan", "Data tidak ditemukan di basis data");
                    }
                }
                Peran peran = peranOpt.get();
                peran.setIsActive(peranRequest.getIsActive());
                peran.setHakAkses(hakAksesList);
                peran.setNama(peranRequest.getNama());
                peran.setDeskripsi(peranRequest.getDeskripsi());
                peran.setKode(peranRequest.getKode());
                peranRepo.save(peran);
                responseDto.setStatus(true);
                responseDto.setMessage("Berhasil menyimpan data");
                responseDto.setPayload(true);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
            }
        }else {
            throw new ResourceNotFoundException("Peran dengan ID " +publicId+ " tidak ditemukan di basis data", "Gagal mengubah Data");
        }
    }

    public ResponseEntity<ResponseDto<List<PeranResponse>>> getPeranReport() {
        sessionFilter.openSessionFilterIsDeleted(false);
        List<Peran> peranList = peranRepo.findByOrderByIdAsc();
        sessionFilter.closeSessionFilterIsDeleted();
        if (peranList.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        } else {
            List<PeranResponse> peranResponses = utils.mapList(peranList, PeranResponse.class);
            ResponseDto<List<PeranResponse>> responseDto = new ResponseDto<>();
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(peranResponses);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseDto<PeranDetailResponse> getPeranByPublicId(UUID publicId) {
        ResponseDto<PeranDetailResponse> responseDto = new ResponseDto<>();
        Optional<Peran> peranOpt = peranRepo.findByPublicId(publicId);
        if (peranOpt.isPresent()) {
            PeranDetailResponse peranDetailResponse = modelMapper.map(peranOpt.get(), PeranDetailResponse.class);
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(peranDetailResponse);
            return responseDto;
        } else {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
    }

    public ResponseDto<List<PeranResponse>> getPeran(Optional<Specification<Peran>> specificationOpt, Sort sort) {
        ResponseDto<List<PeranResponse>> responseDto = new ResponseDto<>();
        List<Peran> peran = new ArrayList<>();
        if (specificationOpt.isPresent()) {
            peran = peranRepo.findAll(specificationOpt.get(), sort);
        } else {
            peran = peranRepo.findAll(sort);
        }
        if (peran.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        List<PeranResponse> peranResponsePage = utils.mapList(peran, PeranResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Peran ditemukan");
        responseDto.setPayload(peranResponsePage);
        return responseDto;
    }

    public ResponseDto<CustomPage<PeranResponse>> getPeranPageable(Optional<Specification<Peran>> specificationOpt, Pageable paging) {
        ResponseDto<CustomPage<PeranResponse>> responseDto = new ResponseDto<>();
        Page<Peran> peran = new PageImpl<>(new ArrayList<>());
        if (specificationOpt.isPresent()) {
            peran = peranRepo.findAll(specificationOpt.get(), paging);
        } else {
            peran = peranRepo.findAll(paging);
        }
        if (peran.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<PeranResponse> peranResponsePage = utils.mapEntityPageIntoDtoPage(peran, PeranResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Peran ditemukan");
        responseDto.setPayload(peranResponsePage);
        return responseDto;
    }
}
