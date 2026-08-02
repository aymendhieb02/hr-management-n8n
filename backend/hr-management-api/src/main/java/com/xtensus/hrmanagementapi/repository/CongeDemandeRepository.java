package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeDemandeRepository extends JpaRepository<CongeDemande, Long> {

    List<CongeDemande> findByEmployeIdOrderByDateSoumissionDesc(Long employeId);

    List<CongeDemande> findByDecideurIdOrderByDateSoumissionDesc(Long decideurId);

    List<CongeDemande> findByStatutLibelle(String libelle);
}
