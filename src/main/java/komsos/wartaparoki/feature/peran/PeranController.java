package komsos.wartaparoki.feature.peran;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import komsos.wartaparoki.feature.peran.model.PeranDetailResponse;
import komsos.wartaparoki.feature.peran.model.PeranRequest;
import komsos.wartaparoki.feature.peran.model.PeranResponse;
import komsos.wartaparoki.helper.CustomPage;
import komsos.wartaparoki.helper.GenericAndSpesification;
import komsos.wartaparoki.helper.GenericOrSpesification;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.helper.SearchCriteria;
import komsos.wartaparoki.helper.SearchOperation;
import komsos.wartaparoki.helper.SessionFilter;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/peran")
@RequiredArgsConstructor
public class PeranController {
    private final SessionFilter sessionFilter;
    private final PeranService peranService;

    private final List<String> avaliableListFieldSort = List.of("nama", "createdAt");
    private final List<String> avaliableListFieldSearch = List.of("nama", "kode", "deskripsi");
    private final String PERMISSION = "AU_PER";

    private Boolean checkAvaliableFieldSort(String string) {
        return avaliableListFieldSort.contains(string);
    }

    private Boolean checkAvaliableFieldSearch(String string) {
        return avaliableListFieldSearch.contains(string);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('"+PERMISSION+"_C')")
    public ResponseEntity<ResponseDto<Boolean>> tambahPeran(
            @RequestBody @Valid PeranRequest peranRequest,
            @Parameter(hidden = true) final Errors errors,
            @Parameter(hidden = true) final UriComponentsBuilder uriComponentsBuilder) {
        if (errors.hasErrors()) {
            ResponseDto<Boolean> responseDto = new ResponseDto<>();
            for (ObjectError error : errors.getAllErrors()) {
                responseDto.getErrorMessage().add(error.getDefaultMessage());
            }
            responseDto.setStatus(false);
            responseDto.setPayload(false);
            responseDto.setMessage("Gagal simpan Peran");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
        return peranService.tambahPeran(peranRequest);
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAuthority('"+PERMISSION+"_R')")
    public ResponseEntity<ResponseDto<CustomPage<PeranResponse>>> getPeranPageable(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Halaman dimulai dari 1") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam Optional<Boolean> isActive,
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
        Optional<Specification<Peran>> specificationOpt = queryProcessing(search, searchBy, searchOperation);
        sessionFilter.openSessionFilterIsDeletedAndIsActive(false, isActive);
        ResponseDto<CustomPage<PeranResponse>> responseEntity = peranService.getPeranPageable(specificationOpt, paging);
        sessionFilter.closeSessionFilterIsDeletedAndIsActive();
        return ResponseEntity.ok(responseEntity);
    }

    @GetMapping()
    public ResponseEntity<ResponseDto<List<PeranResponse>>> getPeran(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam Optional<Boolean> isActive,
            @RequestParam Optional<String> search,
            @RequestParam Optional<String> searchBy,
            @RequestParam(defaultValue = "MATCH") SearchOperation searchOperation) {
        if (!checkAvaliableFieldSort(sortBy)) {
            throw new IllegalArgumentException("Pengurutan dengan " + sortBy + " tidak terdaftar di Sistem");
        }
        Sort sort = Sort.by(sortBy).ascending();
        if (direction.equalsIgnoreCase("DESC")) {
            sort = Sort.by(sortBy).descending();
        }

        Optional<Specification<Peran>> specificationOpt = queryProcessing(search, searchBy, searchOperation);
        sessionFilter.openSessionFilterIsDeletedAndIsActive(false, isActive);
        ResponseDto<List<PeranResponse>> responseEntity = peranService.getPeran(specificationOpt, sort);
        sessionFilter.closeSessionFilterIsDeletedAndIsActive();
        return ResponseEntity.ok(responseEntity);
    }

    private Optional<Specification<Peran>> queryProcessing(Optional<String> search, Optional<String> searchBy, SearchOperation searchOperation) {
        Optional<Specification<Peran>> specificationOpt = Optional.empty();
        GenericAndSpesification<Peran> spesificationAnd = new GenericAndSpesification<>();
        Specification<Peran> specification = null;
        if (search.isPresent()) {
            GenericOrSpesification<Peran> spesificationSearch = new GenericOrSpesification<>();
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
            specification = Specification.where(spesificationAnd).and(spesificationSearch);
        } else {
            specification = Specification.where(spesificationAnd);
        }
        specificationOpt = Optional.ofNullable(specification);
        return specificationOpt;
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ResponseDto<PeranDetailResponse>> getPeranByPublicId(@PathVariable("publicId") UUID publicId) {
        return ResponseEntity.ok(peranService.getPeranByPublicId(publicId));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasAuthority('"+PERMISSION+"_D')")
    public ResponseEntity<ResponseDto<Boolean>> hapusPeranByPublicId(@PathVariable("publicId") UUID publicId) {
        return peranService.hapusPeranByPublicId(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAuthority('"+PERMISSION+"_U')")
    public ResponseEntity<ResponseDto<Boolean>> ubahPeranByPublicId(
            @PathVariable("publicId") UUID publicId,
            @RequestBody @Valid PeranRequest peranRequest,
            @Parameter(hidden = true) final Errors errors,
            @Parameter(hidden = true) final UriComponentsBuilder uriComponentsBuilder) {
        if (errors.hasErrors()) {
            ResponseDto<Boolean> responseDto = new ResponseDto<>();
            for (ObjectError error : errors.getAllErrors()) {
                responseDto.getErrorMessage().add(error.getDefaultMessage());
            }
            responseDto.setStatus(false);
            responseDto.setPayload(false);
            responseDto.setMessage("Gagal simpan Peran");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
        return peranService.ubahPeranByPublicId(publicId, peranRequest);
    }
}
