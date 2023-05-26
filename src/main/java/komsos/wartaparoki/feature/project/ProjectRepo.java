package komsos.wartaparoki.feature.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepo extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project>{
    Optional<Project> findByNama(String nama);

    @Query("FROM Project WHERE id=:id")
    Optional<Project> findById(@Param("id") Long id);

    List<Project> findByOrderByIdAsc();

    Optional<Project> findByKode(String nama);

    Optional<Project> findByPublicId(UUID publicId);
}
