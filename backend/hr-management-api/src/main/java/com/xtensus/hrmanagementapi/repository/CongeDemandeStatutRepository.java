package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeStatut;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeDemandeStatutRepository extends JpaRepository<CongeDemandeStatut, Long> {

    boolean existsByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCaseAndIdNot(String libelle, Long id);

    List<CongeDemandeStatut> findByActifTrue();

    Optional<CongeDemandeStatut> findByLibelle(String libelle);
}

