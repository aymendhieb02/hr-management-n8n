package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

    Optional<Employe> findByEmailIgnoreCase(String email);

    Optional<Employe> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Employe> findByActifTrue();

    List<Employe> findByManagerId(Long managerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE employes SET mot_de_passe_hash = DEFAULT(mot_de_passe_hash), date_modification = CURRENT_TIMESTAMP(6) WHERE employe_id = :id", nativeQuery = true)
    int resetPasswordToDatabaseDefault(@Param("id") Long id);
}
