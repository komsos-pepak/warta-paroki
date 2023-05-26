package komsos.wartaparoki.feature.hakAkses;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import komsos.wartaparoki.feature.hakAkses.model.HakAksesDetailResponse;
import komsos.wartaparoki.feature.hakAkses.model.HakAksesRequest;
import komsos.wartaparoki.feature.hakAkses.model.HakAksesResponse;
import komsos.wartaparoki.feature.modul.Modul;
import komsos.wartaparoki.feature.modul.ModulRepo;
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
@RequiredArgsConstructor
@Slf4j
public class HakAksesService {
    private final HakAksesRepo hakAksesRepo;
    private final ModulRepo modulRepo;
    private final ModelMapper modelMapper;
    private final SessionFilter sessionFilter;
    private final Utils utils;

    public Optional<HakAkses> findByKode(String kode) {
        return hakAksesRepo.findByKode(kode);
    }

    public HakAksesResponse hakAksesInitSave(HakAksesRequest hakAksesResquest) {
        log.info("Create new Permission {} Init", hakAksesResquest.getNama());
        HakAkses hakAkses = new HakAkses();
        Modul modul = modulRepo.findByPublicId(hakAksesResquest.getModulPublicId()).get();
        hakAkses.setNama(hakAksesResquest.getNama());
        hakAkses.setKode(hakAksesResquest.getKode());
        hakAkses.setDeskripsi(hakAksesResquest.getDeskripsi());
        hakAkses.setPublicId(UUID.randomUUID());
        hakAkses.setModul(modul);
        hakAkses = hakAksesRepo.save(hakAkses);
        HakAksesResponse  hakAksesResponse= modelMapper.map(hakAkses, HakAksesResponse.class);
        return hakAksesResponse;
    }
    
    public ResponseEntity<ResponseDto<Boolean>> tambahHakAkses(HakAksesRequest hakAksesRequest) {
        log.info("tambahHakAkses");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<HakAkses> hakAksesOpt = hakAksesRepo.findByKode(hakAksesRequest.getKode());
        sessionFilter.closeSessionFilterIsDeleted();
        if (hakAksesOpt.isPresent()) {
            throw new DuplicateResourceException("Nama Hak Akses " +hakAksesRequest.getKode()+ " sudah digunakan", "Gagal menambahkan Data");
        }else {
            HakAkses hakAkses = modelMapper.map(hakAksesRequest, HakAkses.class);
            Optional<Modul> modulOpt = modulRepo.findByPublicId(hakAksesRequest.getModulPublicId());
            if (modulOpt.isPresent()) {
                hakAkses.setPublicId(UUID.randomUUID());
                hakAkses.setModul(modulOpt.get());
                hakAksesRepo.save(hakAkses);
                responseDto.setStatus(true);
                responseDto.setMessage("Berhasil menyimpan data");
                responseDto.setPayload(true);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
            } else {
                throw new ResourceNotFoundException("Data Modul tidak ditemukan", "Data tidak ditemukan di basis data");
            }
        }
    }

    public ResponseEntity<ResponseDto<CustomPage<HakAksesResponse>>> searchHakAkses(Pageable paging, GenericOrSpesification<HakAkses> genericSpesification) {
        ResponseDto<CustomPage<HakAksesResponse>> responseDto = new ResponseDto<>();
        Page<HakAkses> hakAksesPage = hakAksesRepo.findAll(genericSpesification, paging);
        if (hakAksesPage.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<HakAksesResponse> hakAksesResponsePage = utils.mapEntityPageIntoDtoPage(hakAksesPage, HakAksesResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Hak Akses ditemukan");
        responseDto.setPayload(hakAksesResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<CustomPage<HakAksesResponse>>> getHakAksesPageable(Pageable paging) {
        ResponseDto<CustomPage<HakAksesResponse>> responseDto = new ResponseDto<>();
        Page<HakAkses> hakAkses = hakAksesRepo.findAll(paging);
        if (hakAkses.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<HakAksesResponse> hakAksesResponsePage = utils.mapEntityPageIntoDtoPage(hakAkses, HakAksesResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Hak Akses ditemukan");
        responseDto.setPayload(hakAksesResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<Boolean>> hapusHakAksesById(UUID publicId) {
        log.info("hapusHakAksesById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Optional<HakAkses> hakAksesOpt = hakAksesRepo.findByPublicId(publicId);
        if (hakAksesOpt.isEmpty()) {
            throw new ResourceNotFoundException("Hak Akses dengan ID : " +publicId+ " tidak ditemukan", "Gagal menghapus data");
        }else{
            hakAksesRepo.delete(hakAksesOpt.get());
            responseDto.setStatus(true);
            responseDto.setMessage("Data berhasil dihapus");
            responseDto.setPayload(true);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseEntity<ResponseDto<Boolean>> ubahHakAksesById(UUID publicId, HakAksesRequest hakAksesRequest) {
        log.info("ubahHakAksesById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<HakAkses> hakAksesOpt = hakAksesRepo.findByPublicId(publicId);
        sessionFilter.closeSessionFilterIsDeleted();
        if (hakAksesOpt.isPresent()) {
            sessionFilter.openSessionFilterIsDeleted(false);
            Optional<HakAkses> checkKodeHakAksesOpt = hakAksesRepo.findByKode((hakAksesRequest.getKode()));
            if (checkKodeHakAksesOpt.isPresent() && !checkKodeHakAksesOpt.get().getPublicId().equals(publicId)) {
                throw new DuplicateResourceException("Hak Akses dengan kode : " +hakAksesRequest.getKode()+ " sudah digunakan", "Gagal mengubah Data");
            } else {
                HakAkses hakAkses = hakAksesOpt.get();
                Optional<Modul> modulOpt = modulRepo.findByPublicId(hakAksesRequest.getModulPublicId());
                if (modulOpt.isPresent()) {
                    hakAkses.setNama(hakAksesRequest.getNama());
                    hakAkses.setModul(modulOpt.get());
                    hakAkses.setIsActive(hakAksesRequest.getIsActive());
                    hakAkses.setDeskripsi((hakAksesRequest.getDeskripsi()));
                    hakAkses.setKode(hakAksesRequest.getKode());
                    hakAksesRepo.save(hakAkses);
                    responseDto.setStatus(true);
                    responseDto.setMessage("Berhasil menyimpan data");
                    responseDto.setPayload(true);
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
                } else {
                    throw new ResourceNotFoundException("Data Modul tidak ditemukan", "Data tidak ditemukan di basis data");
                }
            }
        }else {
            throw new ResourceNotFoundException("Hak Akses dengan ID " +publicId+ " tidak ditemukan di basis data", "Gagal mengubah Data");
        }
    }

    public ResponseEntity<ResponseDto<List<HakAksesResponse>>> getHakAksesReport() {
        sessionFilter.openSessionFilterIsDeleted(false);
        List<HakAkses> hakAksesList = hakAksesRepo.findByOrderByIdAsc();
        sessionFilter.closeSessionFilterIsDeleted();
        if (hakAksesList.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        } else {
            List<HakAksesResponse> hakAksesResponses = utils.mapList(hakAksesList, HakAksesResponse.class);
            ResponseDto<List<HakAksesResponse>> responseDto = new ResponseDto<>();
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(hakAksesResponses);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseDto<HakAksesDetailResponse> getHakAksesById(UUID publicId) {
        ResponseDto<HakAksesDetailResponse> responseDto = new ResponseDto<>();
        Optional<HakAkses> hakAksesOpt = hakAksesRepo.findByPublicId(publicId);
        if (hakAksesOpt.isPresent()) {
            HakAksesDetailResponse hakAksesDetailResponse = modelMapper.map(hakAksesOpt.get(), HakAksesDetailResponse.class);
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(hakAksesDetailResponse);
            return responseDto;
        } else {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
    }

    public ResponseDto<List<HakAksesResponse>> getHakAkses(Optional<Specification<HakAkses>> specificationOpt, Sort sort) {
        ResponseDto<List<HakAksesResponse>> responseDto = new ResponseDto<>();
        List<HakAkses> hakAkses = new ArrayList<>();
        if (specificationOpt.isPresent()) {
            hakAkses = hakAksesRepo.findAll(specificationOpt.get(), sort);
        } else {
            hakAkses = hakAksesRepo.findAll(sort);
        }
        if (hakAkses.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        List<HakAksesResponse> hakAksesResponsePage = utils.mapList(hakAkses, HakAksesResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Hak Akses ditemukan");
        responseDto.setPayload(hakAksesResponsePage);
        return responseDto;
    }

    public ResponseDto<CustomPage<HakAksesResponse>> getHakAksesPageable(Optional<Specification<HakAkses>> specificationOpt, Pageable paging) {
        ResponseDto<CustomPage<HakAksesResponse>> responseDto = new ResponseDto<>();
        Page<HakAkses> hakAkses = new PageImpl<>(new ArrayList<>());
        if (specificationOpt.isPresent()) {
            hakAkses = hakAksesRepo.findAll(specificationOpt.get(), paging);
        } else {
            hakAkses = hakAksesRepo.findAll(paging);
        }
        if (hakAkses.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<HakAksesResponse> hakAksesResponsePage = utils.mapEntityPageIntoDtoPage(hakAkses, HakAksesResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Hak Akses ditemukan");
        responseDto.setPayload(hakAksesResponsePage);
        return responseDto;
    }
}
