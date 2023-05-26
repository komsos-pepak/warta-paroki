package komsos.wartaparoki.feature.modul;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModulRepo extends JpaRepository<Modul, Long>, JpaSpecificationExecutor<Modul> {

    Optional<Modul> findByNama(String nama);

    @Query("FROM Modul WHERE id=:id")
    Optional<Modul> findById(@Param("id") Long id);

    List<Modul> findByOrderByIdAsc();

    Optional<Modul> findByKode(String nama);

    Optional<Modul> findByPublicId(UUID publicId);
}
