package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.Raison;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RaisonRepository extends JpaRepository<Raison, Long> {
    List<Raison> findByDisponibleTrueOrderByCommentaireAsc();
}
