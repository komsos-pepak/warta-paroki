package komsos.wartaparoki.feature.pengguna;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import komsos.wartaparoki.feature.pengguna.interfaceClass.ModuleKodeInterface;

public interface PenggunaRepository extends JpaRepository<Pengguna, Long>, JpaSpecificationExecutor<Pengguna> {

    Optional<Pengguna> findByPublicId(UUID publicId);

    Optional<Pengguna> findByUsername(String username);

    @Query(
        value = "SELECT p2.kode as projectKode FROM pengguna p "+
        " LEFT JOIN pengguna_peran pw ON pw.pengguna_id = p.id "+
        " LEFT JOIN peran w ON w.id = pw.peran_id "+
        " LEFT JOIN peran_hak_akses wha ON wha.peran_id = w.id "+
        " LEFT JOIN hak_akses ha ON ha.id = wha.hak_akses_id "+
        " LEFT JOIN modul m ON m.id = ha.modul_id  "+
        " LEFT JOIN project p2 ON p2.id = m.project_id "+
        " WHERE p.id = :id "+
        " GROUP BY p2.id ", 
        nativeQuery = true)
    List<ModuleKodeInterface> getModuls(@Param("id") Long id);
}
