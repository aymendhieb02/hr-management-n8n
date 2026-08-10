package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CongeDemandeRepository extends JpaRepository<CongeDemande, Long> {

    List<CongeDemande> findByEmployeIdOrderByDateSoumissionDesc(Long employeId);

    List<CongeDemande> findByDecideurIdOrderByDateSoumissionDesc(Long decideurId);

    List<CongeDemande> findByStatutLibelle(String libelle);

    @Query("""
            select (count(d) > 0) from CongeDemande d
            where d.employe.id = :employeId
              and (:demandeId is null or d.id <> :demandeId)
              and d.statut.libelle in ('EN_ATTENTE', 'APPROUVEE')
              and d.dateDebut <= :dateFin
              and d.dateFin >= :dateDebut
            """)
    boolean existsChevauchementActif(
            @Param("employeId") Long employeId,
            @Param("demandeId") Long demandeId,
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin
    );
}
