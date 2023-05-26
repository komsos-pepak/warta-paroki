package komsos.wartaparoki.feature.pengguna;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import komsos.wartaparoki.config.security.oauth.CustomOAuth2User;
import komsos.wartaparoki.exception.CustomUnauthorizedException;
import komsos.wartaparoki.exception.DuplicateResourceException;
import komsos.wartaparoki.exception.ResourceNotFoundException;
import komsos.wartaparoki.feature.hakAkses.HakAkses;
import komsos.wartaparoki.feature.pengguna.model.AuthenticationType;
import komsos.wartaparoki.feature.pengguna.model.DetailPenggunaResponse;
import komsos.wartaparoki.feature.pengguna.model.PenggunaAuth;
import komsos.wartaparoki.feature.pengguna.model.PenggunaRequest;
import komsos.wartaparoki.feature.pengguna.model.PenggunaResponse;
import komsos.wartaparoki.feature.pengguna.model.UpdatePenggunaRequest;
import komsos.wartaparoki.feature.peran.Peran;
import komsos.wartaparoki.feature.peran.PeranRepo;
import komsos.wartaparoki.helper.CustomPage;
import komsos.wartaparoki.helper.ResponseDto;
import komsos.wartaparoki.helper.SessionFilter;
import komsos.wartaparoki.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "penggunaCache")
public class PenggunaService implements UserDetailsService{
    private final PenggunaRepository penggunaRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final PeranRepo peranRepository;
    private final Utils utils;
    private final SessionFilter sessionFilter;

    private final String REDIS_CACHE_PENGGUNA="pengguna";
    
    public List<Pengguna> findAll() {
        return penggunaRepository.findAll();
    }

    public void addPeranToPengguna(UUID publicId, List<Long> listIdPeran) {
        Pengguna pengguna = penggunaRepository.findByPublicId(publicId).get();
        Set<Peran> peranList = new HashSet<>();
        for (Long idPeran : listIdPeran) {
            Optional<Peran> peranOpt = peranRepository.findById(idPeran);
            if (peranOpt.isPresent()) {
                peranList.add(peranOpt.get());
            }
        }
        pengguna.setPeran(peranList);
        penggunaRepository.save(pengguna);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Pengguna> user = penggunaRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.error("User not found");
            throw new UsernameNotFoundException("User not found in the database");
        }else {
            log.info("User found in the database :{}", username);
        }
        PenggunaAuth userTampAuth = new PenggunaAuth(user.get());
        return userTampAuth;
    }

    private List<String> getPrivileges(Collection<Peran> roles) {
        List<String> hakAksesList = new ArrayList<>();
        for (Peran role : roles) {
            for (HakAkses hakAkses : role.getHakAkses()) {
                if (!hakAksesList.contains(hakAkses.getKode())) {
                    hakAksesList.add(hakAkses.getKode());
                }
            }
        }
        return hakAksesList;
    }

    private List<String> getWewenang(Collection<Peran> roles) {
        List<String> wewenangList = new ArrayList<>();
        for (Peran role : roles) {
            wewenangList.add(role.getKode());
        }
        return wewenangList;
    }

    @Cacheable(cacheNames = REDIS_CACHE_PENGGUNA, key = "'publicId=' + {#publicId}", unless = "#result == null")
    public DetailPenggunaResponse findByPublicIdPrincipal(UUID publicId) {
        log.info("Get Pengguna di basis data");
        Pengguna pengguna = penggunaRepository.findByPublicId(publicId).get();
        DetailPenggunaResponse penggunaDto = modelMapper.map(pengguna, DetailPenggunaResponse.class);
        penggunaDto.setHakAkses(getPrivileges(pengguna.getPeran()));
        penggunaDto.setWewenang(getWewenang(pengguna.getPeran()));
        return penggunaDto;
    } 

    public Optional<Pengguna> findByPublicId(UUID publicId) {
        log.info("Get Pengguna di basis data");
        Optional<Pengguna> penggunaOpt = penggunaRepository.findByPublicId(publicId);
        return penggunaOpt;
    }

    public ResponseDto<List<PenggunaResponse>> getPengguna(Optional<Specification<Pengguna>> specificationOpt, Sort sort) {
        ResponseDto<List<PenggunaResponse>> responseDto = new ResponseDto<>();
        List<Pengguna> pengguna = new ArrayList<>();
        if (specificationOpt.isPresent()) {
            pengguna = penggunaRepository.findAll(specificationOpt.get(), sort);
        } else {
            pengguna = penggunaRepository.findAll(sort);
        }
        if (pengguna.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        List<PenggunaResponse> penggunaResponsePage = utils.mapList(pengguna, PenggunaResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Pengguna ditemukan");
        responseDto.setPayload(penggunaResponsePage);
        return responseDto;
    }

    public ResponseDto<CustomPage<PenggunaResponse>> getPenggunaPageable(Optional<Specification<Pengguna>> specificationOpt, Pageable paging) {
        ResponseDto<CustomPage<PenggunaResponse>> responseDto = new ResponseDto<>();
        Page<Pengguna> pengguna = new PageImpl<>(new ArrayList<>());
        if (specificationOpt.isPresent()) {
            pengguna = penggunaRepository.findAll(specificationOpt.get(), paging);
        } else {
            pengguna = penggunaRepository.findAll(paging);
        }
        if (pengguna.getContent().isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan", "Data tidak ditemukan di basis data");
        }
        CustomPage<PenggunaResponse> penggunaResponsePage = utils.mapEntityPageIntoDtoPage(pengguna, PenggunaResponse.class);
        responseDto.setStatus(true);
        responseDto.setMessage("Data Pengguna ditemukan");
        responseDto.setPayload(penggunaResponsePage);
        return responseDto;
    }

    public ResponseDto<Boolean> tambahPengguna(PenggunaRequest penggunaRequest) {
        Pengguna penggunaAuth = (Pengguna) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Pengguna> penggunaOpt = penggunaRepository.findByUsername(penggunaRequest.getUsername());
        if (penggunaOpt.isEmpty()) {
            Pengguna  pengguna = modelMapper.map(penggunaRequest, Pengguna.class);
            pengguna.setPublicId(UUID.randomUUID());
            pengguna.setPassword(passwordEncoder.encode(pengguna.getPassword()));
            Set<Peran> peranList = new HashSet<>();
            for (UUID peranPublicId : penggunaRequest.getListPeranPublicId()) {
                Optional<Peran> peranOpt =  peranRepository.findByPublicId(peranPublicId);
                if (peranOpt.isPresent()) {
                    peranList.add(peranOpt.get());
                }
            }
            pengguna.setPeran(peranList);
            pengguna.setPasswordKedaluwarsa(LocalDateTime.now().plusYears(3));
            pengguna.setCreatedBy(penggunaRepository.findById(penggunaAuth.getId()).get());
            pengguna.setUpdatedBy(penggunaRepository.findById(penggunaAuth.getId()).get());
            penggunaRepository.save(pengguna);
            ResponseDto<Boolean> responseDto = new ResponseDto<>();
            responseDto.setStatus(true);
            responseDto.setMessage("Data pengguna berhasil ditambahkan");
            responseDto.setPayload(true);
            return responseDto;
        } else {
            throw new DuplicateResourceException("Username " + penggunaRequest.getUsername() + " sudah digunakan", "Gagal menambahkan data pengguna");
        }
    }

    @Caching(evict = {
        @CacheEvict(value = REDIS_CACHE_PENGGUNA,  key = "'publicId=' + {#publicId}")
    })
    public ResponseDto<Boolean> ubahPenggunaById(UUID publicId, UpdatePenggunaRequest updatePenggunaRequest) {
        log.info("ubahPenggunaById");
        Pengguna penggunaAuth = (Pengguna) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        sessionFilter.openSessionFilterIsDeleted(false);
        Optional<Pengguna> penggunaOpt = penggunaRepository.findByPublicId(publicId);
        sessionFilter.closeSessionFilterIsDeleted();
        if (penggunaOpt.isPresent()) {
            sessionFilter.openSessionFilterIsDeleted(false);
            Optional<Pengguna> checkKodePenggunaOpt = penggunaRepository.findByUsername(updatePenggunaRequest.getUsername());
            if (checkKodePenggunaOpt.isPresent() && !checkKodePenggunaOpt.get().getPublicId().equals(publicId)) {
                throw new DuplicateResourceException("Pengguna dengan username : " +updatePenggunaRequest.getUsername()+ " sudah digunakan", "Gagal mengubah Data");
            } else {
                Set<Peran> peranList = new HashSet<>();
                for (UUID peranPublicId : updatePenggunaRequest.getListPeranPublicId()) {
                    Optional<Peran> hakAksesOpt = peranRepository.findByPublicId(peranPublicId);
                    if (hakAksesOpt.isPresent()) {
                        peranList.add(hakAksesOpt.get());
                    } else {
                        throw new ResourceNotFoundException("Data peran tidak ditemukan", "Data tidak ditemukan di basis data");
                    }
                }
                Pengguna pengguna = penggunaOpt.get();
                pengguna.setIsLocked(updatePenggunaRequest.getIsLocked());
                pengguna.setNama(updatePenggunaRequest.getNama());
                pengguna.setUsername(updatePenggunaRequest.getUsername());
                pengguna.setPeran(peranList);
                pengguna.setUpdatedBy(penggunaRepository.findById(penggunaAuth.getId()).get());
                penggunaRepository.save(pengguna);
                responseDto.setStatus(true);
                responseDto.setMessage("Berhasil menyimpan data");
                responseDto.setPayload(true);
                return responseDto;
            }
        }else {
            throw new ResourceNotFoundException("Pengguna dengan ID " +publicId+ " tidak ditemukan di basis data", "Gagal mengubah Data");
        }
    }

    public Pengguna updateAuthenticationType(String username, String oauth2ClientName) {
    	AuthenticationType authType = AuthenticationType.valueOf(oauth2ClientName.toUpperCase());
        Optional<Pengguna> penggunaOpt = penggunaRepository.findByUsername(username);
        if (penggunaOpt.isPresent()) {
            Pengguna pengguna = penggunaOpt.get();
            pengguna.setAuthType(authType);
            penggunaRepository.save(pengguna);
            return pengguna;
        } else {
            throw new CustomUnauthorizedException("Pengguna tidak terdaftar di sistem", "Anda tidak memiliki akses data");
        }
    }

    public Pengguna createUserByGoogleOauth(CustomOAuth2User oauth2User) {
        String oauth2ClientName = oauth2User.getOauth2ClientName();
		String username = oauth2User.getEmail();
        Pengguna pengguna = new Pengguna();
        pengguna.setAuthType(AuthenticationType.valueOf(oauth2ClientName.toUpperCase()));
        pengguna.setIsLocked(false);
        pengguna.setPublicId(UUID.randomUUID());
        pengguna.setNama(oauth2User.getName());
        pengguna.setUsername(username);
        penggunaRepository.save(pengguna);
        return pengguna;
    }	
}
