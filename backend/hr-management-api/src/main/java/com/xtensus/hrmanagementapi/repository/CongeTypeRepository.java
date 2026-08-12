package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeTypeRepository extends JpaRepository<CongeType, Long> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);

    List<CongeType> findByActifTrue();
    Optional<CongeType> findFirstByNomNotContainingIgnoreCaseOrderByIdAsc(String texte);
}
