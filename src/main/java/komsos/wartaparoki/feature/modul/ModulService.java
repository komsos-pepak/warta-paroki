package komsos.wartaparoki.feature.modul;

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
import komsos.wartaparoki.feature.modul.model.ModulDetailResponse;
import komsos.wartaparoki.feature.modul.model.ModulHakAksesRequest;
import komsos.wartaparoki.feature.modul.model.ModulRequest;
import komsos.wartaparoki.feature.modul.model.ModulResponse;
import komsos.wartaparoki.feature.project.Project;
import komsos.wartaparoki.feature.project.ProjectRepo;
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
public class ModulService {
    private final ModelMapper modelMapper;
    private final ModulRepo modulRepo;
    private final ProjectRepo projectRepo;
    private final SessionFilter sessionFilter;
    private final Utils utils;

    public ResponseEntity<ResponseDto<Boolean>> tambahModul(ModulRequest modulRequest) {
        log.info("tambahModul");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Modul> modulOpt = modulRepo.findByKode(modulRequest.getKode());
        sessionFilter.closeSessionFilterIsDeleted();
        if (modulOpt.isPresent()) {
            throw new DuplicateResourceException("Kode modul " +modulRequest.getKode()+ " sudah digunakan", "Gagal menambahkan Data");
        }else {
            sessionFilter.openSessionFilterIsDeleted(false);
            Optional<Project> projectOpt = projectRepo.findByPublicId(modulRequest.getProjectPublicId());
            sessionFilter.closeSessionFilterIsDeleted();
            if (projectOpt.isPresent()) {
                Modul modul = modelMapper.map(modulRequest, Modul.class);
                if (modulRequest.getHakAkses() != null) {
                    if (!modulRequest.getHakAkses().isEmpty()) {
                        Set<HakAkses> hakAksesSet = new HashSet<>();
                        for (ModulHakAksesRequest modulHakAksesRequest : modulRequest.getHakAkses()) {
                            HakAkses hakAkses =  new HakAkses();
                            hakAkses.setDeskripsi(modulHakAksesRequest.getDeskripsi());
                            hakAkses.setPublicId(UUID.randomUUID());
                            hakAkses.setIsActive(true);
                            hakAkses.setIsDeleted(false);
                            hakAkses.setKode(modulHakAksesRequest.getKode());
                            hakAkses.setNama(modulHakAksesRequest.getNama());
                            hakAkses.setModul(modul);
                            hakAksesSet.add(hakAkses);
                        }
                        modul.setHakAkses(hakAksesSet);
                    }
                }
                modul.setProject(projectOpt.get());
                modul.setIsActive(true);
                modul.setPublicId(UUID.randomUUID());
                modulRepo.save(modul);
                responseDto.setStatus(true);
                responseDto.setMessage("Berhasil menyimpan data");
                responseDto.setPayload(true);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
            } else {
                throw new ResourceNotFoundException("Project Modul tidak ditemukan di basis data ", "Gagal simpan data Modul");
            }
        }
    }

    public ResponseEntity<ResponseDto<CustomPage<ModulResponse>>> searchModul(Pageable paging, GenericOrSpesification<Modul> genericSpesification) {
        ResponseDto<CustomPage<ModulResponse>> responseDto = new ResponseDto<>();
        Page<Modul> modulPage = modulRepo.findAll(genericSpesification, paging);
        if (modulPage.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<ModulResponse> modulResponsePage = utils.mapEntityPageIntoDtoPage(modulPage, ModulResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Modul ditemukan");
        responseDto.setPayload(modulResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<CustomPage<ModulResponse>>> getModulPageable(Pageable paging) {
        ResponseDto<CustomPage<ModulResponse>> responseDto = new ResponseDto<>();
        Page<Modul> modul = modulRepo.findAll(paging);
        if (modul.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<ModulResponse> modulResponsePage = utils.mapEntityPageIntoDtoPage(modul, ModulResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Modul ditemukan");
        responseDto.setPayload(modulResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<Boolean>> hapusModulById(UUID publicId) {
        log.info("hapusModulById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Optional<Modul> modulOpt = modulRepo.findByPublicId(publicId);
        if (modulOpt.isEmpty()) {
            throw new ResourceNotFoundException("Modul dengan ID : " +publicId+ " tidak ditemukan", "Gagal menghapus data");
        }else{
            modulRepo.delete(modulOpt.get());
            responseDto.setStatus(true);
            responseDto.setMessage("Data berhasil dihapus");
            responseDto.setPayload(true);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseEntity<ResponseDto<Boolean>> ubahModulById(UUID publicId, ModulRequest modulRequest) {
        log.info("ubahModulById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Modul> modulOpt = modulRepo.findByPublicId(publicId);
        sessionFilter.closeSessionFilterIsDeleted();
        if (modulOpt.isPresent()) {
            sessionFilter.openSessionFilterIsDeleted(false);
            Optional<Modul> checkKodeModulOpt = modulRepo.findByKode(modulRequest.getKode());
            if (checkKodeModulOpt.isPresent() && !checkKodeModulOpt.get().getPublicId().equals(publicId)) {
                throw new DuplicateResourceException("Modul dengan kode : " +modulRequest.getKode()+ " sudah digunakan", "Gagal mengubah Data");
            } else {
                Optional<Project> projectOpt = projectRepo.findByPublicId(modulRequest.getProjectPublicId());
                sessionFilter.closeSessionFilterIsDeleted();
                if (projectOpt.isPresent()) {
                    Modul modul = modulOpt.get();
                    modul.setNama(modulRequest.getNama());
                    modul.setIsActive(modulRequest.getIsActive());
                    modul.setKode(modulRequest.getKode());
                    modul.setDeskripsi(modulRequest.getDeskripsi());
                    modul.setProject(projectOpt.get());
                    modulRepo.save(modul);
                    responseDto.setStatus(true);
                    responseDto.setMessage("Berhasil menyimpan data");
                    responseDto.setPayload(true);
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
                } else {
                    throw new ResourceNotFoundException("Project Modul tidak ditemukan di basis data ", "Gagal mengubah Data");
                }
            }
        }else {
            throw new ResourceNotFoundException("Modul dengan ID " +publicId+ " tidak ditemukan di basis data", "Gagal mengubah Data");
        }
    }

    public ResponseEntity<ResponseDto<List<ModulResponse>>> getModulReport() {
        sessionFilter.openSessionFilterIsDeleted(false);
        List<Modul> modulList = modulRepo.findByOrderByIdAsc();
        sessionFilter.closeSessionFilterIsDeleted();
        if (modulList.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        } else {
            List<ModulResponse> modulResponses = utils.mapList(modulList, ModulResponse.class);
            ResponseDto<List<ModulResponse>> responseDto = new ResponseDto<>();
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(modulResponses);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseDto<ModulDetailResponse> getModulByPublicId(UUID publicId) {
        ResponseDto<ModulDetailResponse> responseDto = new ResponseDto<>();
        Optional<Modul> modulOpt = modulRepo.findByPublicId(publicId);
        if (modulOpt.isPresent()) {
            ModulDetailResponse modulResponse = modelMapper.map(modulOpt.get(), ModulDetailResponse.class);
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(modulResponse);
            return responseDto;
        } else {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
    }

    public ResponseDto<List<ModulResponse>> getModul(Optional<Specification<Modul>> specificationOpt, Sort sort) {
        ResponseDto<List<ModulResponse>> responseDto = new ResponseDto<>();
        List<Modul> modul = new ArrayList<>();
        if (specificationOpt.isPresent()) {
            modul = modulRepo.findAll(specificationOpt.get(), sort);
        } else {
            modul = modulRepo.findAll(sort);
        }
        if (modul.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        List<ModulResponse> modulResponsePage = utils.mapList(modul, ModulResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Modul ditemukan");
        responseDto.setPayload(modulResponsePage);
        return responseDto;
    }

    public ResponseDto<CustomPage<ModulResponse>> getModulPageable(Optional<Specification<Modul>> specificationOpt, Pageable paging) {
        ResponseDto<CustomPage<ModulResponse>> responseDto = new ResponseDto<>();
        Page<Modul> modul = new PageImpl<>(new ArrayList<>());
        if (specificationOpt.isPresent()) {
            modul = modulRepo.findAll(specificationOpt.get(), paging);
        } else {
            modul = modulRepo.findAll(paging);
        }
        if (modul.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<ModulResponse> modulResponsePage = utils.mapEntityPageIntoDtoPage(modul, ModulResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Modul ditemukan");
        responseDto.setPayload(modulResponsePage);
        return responseDto;
    }
}
