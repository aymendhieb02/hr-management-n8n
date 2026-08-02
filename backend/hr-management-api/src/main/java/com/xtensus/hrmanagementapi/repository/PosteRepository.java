package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosteRepository extends JpaRepository<Poste, Long> {

    boolean existsByIntituleIgnoreCase(String intitule);

    boolean existsByIntituleIgnoreCaseAndIdNot(String intitule, Long id);
}
