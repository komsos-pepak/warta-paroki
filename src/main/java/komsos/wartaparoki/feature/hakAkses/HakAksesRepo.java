package komsos.wartaparoki.feature.hakAkses;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HakAksesRepo extends JpaRepository<HakAkses, Long>, JpaSpecificationExecutor<HakAkses>{

    Optional<HakAkses> findByKode(String kode);
    
    Optional<HakAkses> findByNama(String nama);

    @Query("FROM HakAkses WHERE id=:id")
    Optional<HakAkses> findById(@Param("id") Long id);

    List<HakAkses> findByOrderByIdAsc();

    Optional<HakAkses> findByPublicId(UUID publicId);
}
