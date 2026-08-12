package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeSolde;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CongeSoldeRepository extends JpaRepository<CongeSolde, Long> {
    Optional<CongeSolde> findByEmployeIdAndCongeTypeIdAndAnnee(Long employeId, Long congeTypeId, Integer annee);
    boolean existsByEmployeIdAndCongeTypeIdAndAnnee(Long employeId, Long congeTypeId, Integer annee);
    boolean existsByEmployeIdAndCongeTypeIdAndAnneeAndIdNot(Long employeId, Long congeTypeId, Integer annee, Long id);
    List<CongeSolde> findByEmployeId(Long employeId);
    List<CongeSolde> findByAnnee(Integer annee);
    boolean existsByEmployeId(Long employeId);
    @Query(value = "SELECT * FROM conge_soldes WHERE employe_id = :employeId FOR UPDATE", nativeQuery = true)
    Optional<CongeSolde> findFirstWithLockByEmployeId(@Param("employeId") Long employeId);
}
