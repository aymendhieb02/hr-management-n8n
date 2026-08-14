package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeHistorique;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CongeDemandeHistoriqueRepository extends JpaRepository<CongeDemandeHistorique, Long> {
    List<CongeDemandeHistorique> findAllByOrderByDateActionDesc();

    @Query("""
            select h from CongeDemandeHistorique h
            join h.demande d join d.employe e join d.congeType t join d.statut s
            where (:employeId is null or e.id = :employeId)
              and (:employeId is not null or s.libelle <> 'BROUILLON')
              and (:recherche is null or lower(concat(e.prenom, ' ', e.nom)) like lower(concat('%', :recherche, '%'))
                   or lower(h.action) like lower(concat('%', :recherche, '%'))
                   or lower(t.nom) like lower(concat('%', :recherche, '%'))
                   or lower(s.libelle) like lower(concat('%', :recherche, '%'))
                   or cast(d.id as string) like concat('%', :recherche, '%'))
            """)
    Page<CongeDemandeHistorique> rechercher(@Param("recherche") String recherche,
            @Param("employeId") Long employeId, Pageable pageable);

    @Query("select distinct e.id, concat(e.prenom, ' ', e.nom) from CongeDemandeHistorique h join h.demande d join d.employe e join d.statut s where s.libelle <> 'BROUILLON' order by concat(e.prenom, ' ', e.nom)")
    List<Object[]> listerEmployesAvecHistorique();
}
