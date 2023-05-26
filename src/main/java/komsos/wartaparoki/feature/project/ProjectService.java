package komsos.wartaparoki.feature.project;

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
import komsos.wartaparoki.feature.project.model.ProjectDetailResponse;
import komsos.wartaparoki.feature.project.model.ProjectRequest;
import komsos.wartaparoki.feature.project.model.ProjectResponse;
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
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final ModelMapper modelMapper;
    private final SessionFilter sessionFilter;
    private final Utils utils;

    public ResponseEntity<ResponseDto<Boolean>> tambahProject(ProjectRequest projectRequest) {
        log.info("tambahProject");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Project> projectOpt = projectRepo.findByKode(projectRequest.getKode());
        sessionFilter.closeSessionFilterIsDeleted();
        if (projectOpt.isPresent()) {
            throw new DuplicateResourceException("Nama Project " +projectRequest.getKode()+ " sudah digunakan", "Gagal menambahkan Data");
        }else {
            Project project = modelMapper.map(projectRequest, Project.class);
            project.setPublicId(UUID.randomUUID());
            projectRepo.save(project);
            responseDto.setStatus(true);
            responseDto.setMessage("Berhasil menyimpan data");
            responseDto.setPayload(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        }
    }

    public ResponseEntity<ResponseDto<CustomPage<ProjectResponse>>> searchProject(Pageable paging, GenericOrSpesification<Project> genericSpesification) {
        ResponseDto<CustomPage<ProjectResponse>> responseDto = new ResponseDto<>();
        Page<Project> projectPage = projectRepo.findAll(genericSpesification, paging);
        if (projectPage.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<ProjectResponse> projectResponsePage = utils.mapEntityPageIntoDtoPage(projectPage, ProjectResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Project ditemukan");
        responseDto.setPayload(projectResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<CustomPage<ProjectResponse>>> getProjectPageable(Pageable paging) {
        ResponseDto<CustomPage<ProjectResponse>> responseDto = new ResponseDto<>();
        Page<Project> project = projectRepo.findAll(paging);
        if (project.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<ProjectResponse> projectResponsePage = utils.mapEntityPageIntoDtoPage(project, ProjectResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Project ditemukan");
        responseDto.setPayload(projectResponsePage);
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<ResponseDto<Boolean>> hapusProjectByPublicId(UUID publicId) {
        log.info("hapusProjectById");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Optional<Project> projectOpt = projectRepo.findByPublicId(publicId);
        if (projectOpt.isEmpty()) {
            throw new ResourceNotFoundException("Project dengan ID : " +publicId+ " tidak ditemukan", "Gagal menghapus data");
        }else{
            projectRepo.delete(projectOpt.get());
            responseDto.setStatus(true);
            responseDto.setMessage("Data berhasil dihapus");
            responseDto.setPayload(true);
            return ResponseEntity.ok(responseDto);
        }
    }

    public ResponseEntity<ResponseDto<Boolean>> ubahProjectByPublicId(UUID publicId, ProjectRequest projectRequest) {
        log.info("ubahProjectByPublicId");
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Project> projectOpt = projectRepo.findByPublicId(publicId);
        sessionFilter.closeSessionFilterIsDeleted();
        if (projectOpt.isPresent()) {
            sessionFilter.openSessionFilterIsDeleted(false);
            Optional<Project> checkKodeProjectOpt = projectRepo.findByNama(projectRequest.getNama());
            if (checkKodeProjectOpt.isPresent() && !checkKodeProjectOpt.get().getPublicId().equals(publicId)) {
                throw new DuplicateResourceException("Project dengan kode : " +projectRequest.getNama()+ " sudah digunakan", "Gagal mengubah Data");
            } else {
                Project project = projectOpt.get();
                project.setNama(projectRequest.getNama());
                project.setIsActive(projectRequest.getIsActive());
                project.setDeskripsi(projectRequest.getDeskripsi());
                project.setKode(projectRequest.getKode());
                projectRepo.save(project);
                responseDto.setStatus(true);
                responseDto.setMessage("Berhasil menyimpan data");
                responseDto.setPayload(true);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
            }
        }else {
            throw new ResourceNotFoundException("Project dengan ID " +publicId+ " tidak ditemukan di basis data", "Gagal mengubah Data");
        }
    }

    public ResponseDto<ProjectDetailResponse> getProjectByPublicId(UUID publicId) {
        ResponseDto<ProjectDetailResponse> responseDto = new ResponseDto<>();
        Optional<Project> projectOpt = projectRepo.findByPublicId(publicId);
        if (projectOpt.isPresent()) {
            ProjectDetailResponse projectDetailResponse = modelMapper.map(projectOpt.get(), ProjectDetailResponse.class);
            responseDto.setStatus(true);
            responseDto.setMessage("Data ditemukan");
            responseDto.setPayload(projectDetailResponse);
            return responseDto;
        } else {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
    }

    public ResponseDto<List<ProjectResponse>> getProject(Optional<Specification<Project>> specificationOpt, Sort sort) {
        ResponseDto<List<ProjectResponse>> responseDto = new ResponseDto<>();
        List<Project> project = new ArrayList<>();
        if (specificationOpt.isPresent()) {
            project = projectRepo.findAll(specificationOpt.get(), sort);
        } else {
            project = projectRepo.findAll(sort);
        }
        if (project.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        List<ProjectResponse> projectResponsePage = utils.mapList(project, ProjectResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Project ditemukan");
        responseDto.setPayload(projectResponsePage);
        return responseDto;
    }

    public ResponseDto<CustomPage<ProjectResponse>> getProjectPageable(Optional<Specification<Project>> specificationOpt, Pageable paging) {
        ResponseDto<CustomPage<ProjectResponse>> responseDto = new ResponseDto<>();
        Page<Project> project = new PageImpl<>(new ArrayList<>());
        if (specificationOpt.isPresent()) {
            project = projectRepo.findAll(specificationOpt.get(), paging);
        } else {
            project = projectRepo.findAll(paging);
        }
        if (project.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<ProjectResponse> projectResponsePage = utils.mapEntityPageIntoDtoPage(project, ProjectResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Project ditemukan");
        responseDto.setPayload(projectResponsePage);
        return responseDto;
    }
}
