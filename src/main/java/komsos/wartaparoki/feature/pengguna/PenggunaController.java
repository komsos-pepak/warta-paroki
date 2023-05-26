package komsos.wartaparoki.feature.pengguna;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import komsos.wartaparoki.exception.ResourceNotFoundException;
import komsos.wartaparoki.feature.pengguna.model.DetailPenggunaResponse;
import komsos.wartaparoki.feature.pengguna.model.PenggunaRequest;
import komsos.wartaparoki.feature.pengguna.model.PenggunaResponse;
import komsos.wartaparoki.feature.pengguna.model.UpdatePenggunaRequest;
import komsos.wartaparoki.helper.CustomPage;
import komsos.wartaparoki.helper.GenericAndSpesification;
import komsos.wartaparoki.helper.GenericOrSpesification;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.helper.SearchCriteria;
import komsos.wartaparoki.helper.SearchOperation;
import komsos.wartaparoki.helper.SessionFilter;
import komsos.wartaparoki.helper.UserPrincipal;
import komsos.wartaparoki.utils.CookieUtil;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/pengguna")
@RequiredArgsConstructor
public class PenggunaController {
    private final PenggunaService penggunaService;
    private final UserPrincipal userPrincipal;
    private final SessionFilter sessionFilter;
        private final CookieUtil cookieUtil;

    private final List<String> avaliableListFieldSort = List.of("nama", "username");
    private final List<String> avaliableListFieldSearch = List.of("nama", "username");
    private final String PERMISSION = "AU_PENG";

    private Boolean checkAvaliableFieldSort(String string) {
        return avaliableListFieldSort.contains(string);
    }

    private Boolean checkAvaliableFieldSearch(String string) {
        return avaliableListFieldSearch.contains(string);
    }

    @GetMapping("/current-user")
    public ResponseEntity<ResponseDto<DetailPenggunaResponse>> getCurrentUser() {
        DetailPenggunaResponse userDto = userPrincipal.getUserPrincipal();
        ResponseDto<DetailPenggunaResponse> responseDto = new ResponseDto<>();
        responseDto.setStatus(true);
        responseDto.setMessage("User ditemukan");
        responseDto.setPayload(userDto);
        return ResponseEntity.ok().body(responseDto);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ResponseDto<DetailPenggunaResponse>> getPenggunaByPublicId(@PathVariable("publicId") UUID publicId) {
        Optional<DetailPenggunaResponse> penggunaDtoOpt = Optional.ofNullable(penggunaService.findByPublicIdPrincipal(publicId));
        if (penggunaDtoOpt.isPresent()) {
            ResponseDto<DetailPenggunaResponse> responseDto = new ResponseDto<>();
            responseDto.setStatus(true);
            responseDto.setMessage("Data pengguna ditemukan");
            responseDto.setPayload(penggunaDtoOpt.get());
            return ResponseEntity.ok(responseDto);
        } else {
            throw new ResourceNotFoundException("Data pengguna tidak ditemukan", "Data tidak ditemukan");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('"+PERMISSION+"_C')")
    public ResponseEntity<ResponseDto<Boolean>> tambahPengguna(
            @RequestBody @Valid PenggunaRequest penggunaRequest,
            @Parameter(hidden = true) final Errors errors,
            @Parameter(hidden = true) final UriComponentsBuilder uriComponentsBuilder) {
        if (errors.hasErrors()) {
            ResponseDto<Boolean> responseDto = new ResponseDto<>();
            for (ObjectError error : errors.getAllErrors()) {
                responseDto.getErrorMessage().add(error.getDefaultMessage());
            }
            responseDto.setStatus(false);
            responseDto.setPayload(false);
            responseDto.setMessage("Gagal simpan Pengguna");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(penggunaService.tambahPengguna(penggunaRequest));
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAuthority('"+PERMISSION+"_R')")
    public ResponseEntity<ResponseDto<CustomPage<PenggunaResponse>>> getPenggunaPageable(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size,
            @RequestParam(defaultValue = "nama") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam Optional<String> searchBy,
            @RequestParam Optional<String> search,
            @RequestParam(defaultValue = "MATCH") SearchOperation searchOperation) {
        if (!checkAvaliableFieldSort(sortBy)) {
            throw new IllegalArgumentException("Pengurutan dengan " + sortBy + " tidak terdaftar di Sistem");
        }
        Sort sort = Sort.by(sortBy).ascending();
        if (direction.equalsIgnoreCase("DESC")) {
            sort = Sort.by(sortBy).descending();
        }
        Pageable paging = PageRequest.of(page - 1, size, sort);
        Optional<Specification<Pengguna>> specificationOpt = queryProcessing(search, searchBy, searchOperation);
        sessionFilter.openSessionFilterIsDeleted(false);
        ResponseDto<CustomPage<PenggunaResponse>> responseEntity = penggunaService.getPenggunaPageable(specificationOpt, paging);
        sessionFilter.closeSessionFilterIsDeletedAndIsActive();
        return ResponseEntity.ok(responseEntity);
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('"+PERMISSION+"_R')")
    public ResponseEntity<ResponseDto<List<PenggunaResponse>>> getPengguna(
            @RequestParam(defaultValue = "nama") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam Optional<Boolean> isActive,
            @RequestParam Optional<String> search,
            @RequestParam Optional<String> searchBy,
            @RequestParam(defaultValue = "MATCH") SearchOperation searchOperation,
            @RequestParam Optional<Long> projectId) {
        if (!checkAvaliableFieldSort(sortBy)) {
            throw new IllegalArgumentException("Pengurutan dengan " + sortBy + " tidak terdaftar di Sistem");
        }
        Sort sort = Sort.by(sortBy).ascending();
        if (direction.equalsIgnoreCase("DESC")) {
            sort = Sort.by(sortBy).descending();
        }
        Optional<Specification<Pengguna>> specificationOpt = queryProcessing(search, searchBy, searchOperation);
        sessionFilter.openSessionFilterIsDeletedAndIsActive(false, isActive);
        ResponseDto<List<PenggunaResponse>> responseEntity = penggunaService.getPengguna(specificationOpt, sort);
        sessionFilter.closeSessionFilterIsDeletedAndIsActive();
        return ResponseEntity.ok(responseEntity);
    }

    private Optional<Specification<Pengguna>> queryProcessing(Optional<String> search, Optional<String> searchBy, SearchOperation searchOperation) {
        Optional<Specification<Pengguna>> specificationOpt = Optional.empty();
        GenericAndSpesification<Pengguna> spesificationAnd = new GenericAndSpesification<>();
        Specification<Pengguna> specification = null;
        if (search.isPresent()) {
            GenericOrSpesification<Pengguna> spesificationSearch = new GenericOrSpesification<>();
            if (searchBy.isPresent()) {
                if (searchBy.get().equalsIgnoreCase("all")) {
                    for (String avaliableFieldSearch : avaliableListFieldSearch) {
                        spesificationSearch.add(new SearchCriteria(avaliableFieldSearch, search.get(), searchOperation));
                    }
                } else {
                    if (!checkAvaliableFieldSearch(searchBy.get())) {
                        throw new IllegalArgumentException("Pencarian dengan " + searchBy.get() + " tidak terdaftar di Sistem");
                    } else {
                        spesificationSearch.add(new SearchCriteria(searchBy.get(), search.get(), searchOperation));
                    }
                }
            } else {
                String savaliableListFieldSearchString = avaliableListFieldSearch.stream().map(Object::toString).collect(Collectors.joining(", "));
                throw new IllegalArgumentException("Pencarian berdasarkan kata all, " + savaliableListFieldSearchString);
            }
            specification = Specification.where(specification).and(spesificationAnd).and(spesificationSearch);
        } else {
            specification = Specification.where(specification).and(spesificationAnd);
        }
        specificationOpt = Optional.ofNullable(specification);
        return specificationOpt;
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAuthority('"+PERMISSION+"_U')")
    public ResponseEntity<ResponseDto<Boolean>> ubahPenggunaById(
            @PathVariable("publicId") UUID publicId,
            @RequestBody @Valid UpdatePenggunaRequest updatePenggunaRequest,
            @Parameter(hidden = true) final Errors errors,
            @Parameter(hidden = true) final UriComponentsBuilder uriComponentsBuilder) {
        if (errors.hasErrors()) {
            ResponseDto<Boolean> responseDto = new ResponseDto<>();
            for (ObjectError error : errors.getAllErrors()) {
                responseDto.getErrorMessage().add(error.getDefaultMessage());
            }
            responseDto.setStatus(false);
            responseDto.setPayload(false);
            responseDto.setMessage("Gagal simpan Pengguna");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
        return ResponseEntity.ok(penggunaService.ubahPenggunaById(publicId, updatePenggunaRequest));
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) throws StreamWriteException, DatabindException, IOException {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        responseDto.setStatus(true);
        responseDto.setMessage("Berhasil Logout");
        responseDto.setPayload(true);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());
        new ObjectMapper().writeValue(response.getOutputStream(), responseDto);
    }
}
