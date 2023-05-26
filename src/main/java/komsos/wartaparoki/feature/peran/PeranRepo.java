package komsos.wartaparoki.feature.peran;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PeranRepo extends JpaRepository<Peran, Long>, JpaSpecificationExecutor<Peran>{
    @Query("FROM Peran WHERE id=:id")
    Optional<Peran> findById(@Param("id") Long id);

    List<Peran> findByOrderByIdAsc();

    Optional<Peran> findByKode(String kode);

    Optional<Peran> findByPublicId(UUID publicId);
}
