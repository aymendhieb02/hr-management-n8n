package com.xtensus.hrmanagementapi.conge.solde.service;

import com.xtensus.hrmanagementapi.conge.demande.exception.CongeDemandeInvalideException;
import com.xtensus.hrmanagementapi.conge.solde.historique.CongeSoldeHistoriqueService;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.CongeSolde;
import com.xtensus.hrmanagementapi.domain.entity.CongeType;
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.repository.CongeSoldeRepository;
import com.xtensus.hrmanagementapi.repository.CongeTypeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeSoldeTransactionService {
    public static final BigDecimal SOLDE_MINIMUM = new BigDecimal("-5.00");
    private final CongeSoldeRepository soldes;
    private final CongeTypeRepository types;
    private final CongeSoldeHistoriqueService historiques;
    public CongeSoldeTransactionService(CongeSoldeRepository soldes, CongeTypeRepository types, CongeSoldeHistoriqueService historiques) {
        this.soldes=soldes; this.types=types; this.historiques=historiques;
    }

    @Transactional
    public CongeSolde assurerSolde(Employe employe) {
        return soldes.findFirstWithLockByEmployeId(employe.getId()).orElseGet(() -> {
            CongeType type = types.findFirstByNomNotContainingIgnoreCaseOrderByIdAsc("autorisation")
                    .orElseThrow(() -> new CongeDemandeInvalideException("Type de congé principal introuvable"));
            CongeSolde solde = new CongeSolde(); solde.setEmploye(employe); solde.setCongeType(type);
            solde.setAnnee(java.time.Year.now().getValue()); solde.setDroitAcquis(BigDecimal.ZERO.setScale(2));
            solde.setJoursUtilises(BigDecimal.ZERO.setScale(2)); solde.setRestants(BigDecimal.ZERO.setScale(2));
            solde.setDateCreation(LocalDateTime.now()); return soldes.save(solde);
        });
    }

    @Transactional
    public void debiter(CongeDemande demande) {
        if ("AUTORISATION_ABSENCE".equals(demande.getNature())) return;
        String reference="DEMANDE:"+demande.getId(); if (historiques.existe(reference)) return;
        CongeSolde solde=assurerSolde(demande.getEmploye()); BigDecimal montant=demande.getNombreJours().setScale(2,RoundingMode.HALF_UP);
        BigDecimal avant=solde.getRestants(); BigDecimal apres=avant.subtract(montant);
        if (apres.compareTo(SOLDE_MINIMUM)<0) throw new CongeDemandeInvalideException("Solde insuffisant : le solde ne peut pas descendre sous -5 jours");
        solde.setRestants(apres); solde.setJoursUtilises(solde.getJoursUtilises().add(montant)); solde.setDateModification(LocalDateTime.now());
        soldes.save(solde); historiques.enregistrer(solde,demande,"DEBIT_CONGE",montant.negate(),avant,apres,reference);
    }

    @Transactional
    public void crediterMensuellement(Employe employe, BigDecimal montant, String reference) {
        if (historiques.existe(reference)) return;
        CongeSolde solde=assurerSolde(employe); BigDecimal avant=solde.getRestants(); BigDecimal apres=avant.add(montant);
        solde.setRestants(apres); solde.setDroitAcquis(solde.getDroitAcquis().add(montant)); solde.setDateModification(LocalDateTime.now());
        soldes.save(solde); historiques.enregistrer(solde,null,"ACQUISITION_MENSUELLE",montant,avant,apres,reference);
    }
}
