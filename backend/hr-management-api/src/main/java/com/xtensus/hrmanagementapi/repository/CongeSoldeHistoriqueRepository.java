package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeSoldeHistorique;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeSoldeHistoriqueRepository extends JpaRepository<CongeSoldeHistorique, Long> {
    List<CongeSoldeHistorique> findBySoldeIdOrderByDateExecutionDesc(Long soldeId);
}
