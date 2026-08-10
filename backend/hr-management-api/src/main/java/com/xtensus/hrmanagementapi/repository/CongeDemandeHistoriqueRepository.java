package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeHistorique;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeDemandeHistoriqueRepository extends JpaRepository<CongeDemandeHistorique, Long> {
    List<CongeDemandeHistorique> findAllByOrderByDateActionDesc();
}
