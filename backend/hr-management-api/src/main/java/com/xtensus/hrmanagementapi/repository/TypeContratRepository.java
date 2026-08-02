package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.TypeContrat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeContratRepository extends JpaRepository<TypeContrat, Long> {

    boolean existsByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCaseAndIdNot(String libelle, Long id);
}
