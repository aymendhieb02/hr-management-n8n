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
import com.xtensus.hrmanagementapi.variable.VariableSystemeService;

@Service
public class CongeSoldeTransactionService {
    private final CongeSoldeRepository soldes;
    private final CongeTypeRepository types;
    private final CongeSoldeHistoriqueService historiques;
    private final VariableSystemeService variables;
    public CongeSoldeTransactionService(CongeSoldeRepository soldes, CongeTypeRepository types, CongeSoldeHistoriqueService historiques, VariableSystemeService variables) {
        this.soldes=soldes; this.types=types; this.historiques=historiques; this.variables=variables;
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
        BigDecimal minimum = soldeMinimum();
        if (apres.compareTo(minimum)<0) throw new CongeDemandeInvalideException("Solde insuffisant : le solde ne peut pas descendre sous " + minimum.stripTrailingZeros().toPlainString() + " jours");
        solde.setRestants(apres); solde.setJoursUtilises(solde.getJoursUtilises().add(montant)); solde.setDateModification(LocalDateTime.now());
        soldes.save(solde); historiques.enregistrer(solde,demande,"DEBIT_CONGE",montant.negate(),avant,apres,reference);
    }

    @Transactional
    public void validerDisponibilite(Employe employe, BigDecimal joursDemandes, BigDecimal joursReserves) {
        CongeSolde solde = assurerSolde(employe);
        BigDecimal disponibleAvecTolerance = solde.getRestants().subtract(joursReserves).subtract(joursDemandes);
        BigDecimal minimum = soldeMinimum();
        if (disponibleAvecTolerance.compareTo(minimum) < 0) {
            BigDecimal maximumDemandable = solde.getRestants().subtract(minimum).subtract(joursReserves);
            throw new CongeDemandeInvalideException(
                    "Solde insuffisant : vous pouvez demander au maximum "
                            + maximumDemandable.max(BigDecimal.ZERO).stripTrailingZeros().toPlainString()
                            + " jour(s), demandes en attente comprises. Le solde ne peut pas descendre sous "
                            + minimum.stripTrailingZeros().toPlainString() + " jours."
            );
        }
    }

    @Transactional
    public void crediterMensuellement(Employe employe, BigDecimal montant, String reference) {
        if (historiques.existe(reference)) return;
        CongeSolde solde=assurerSolde(employe); BigDecimal avant=solde.getRestants(); BigDecimal apres=avant.add(montant);
        solde.setRestants(apres); solde.setDroitAcquis(solde.getDroitAcquis().add(montant)); solde.setDateModification(LocalDateTime.now());
        soldes.save(solde); historiques.enregistrer(solde,null,"ACQUISITION_MENSUELLE",montant,avant,apres,reference);
    }

    @Transactional
    public void ajusterConsommation(CongeDemande demande, BigDecimal joursReels) {
        if ("AUTORISATION_ABSENCE".equals(demande.getNature())) throw new CongeDemandeInvalideException("Une autorisation d'absence ne debite pas le solde de conge");
        if (joursReels == null || joursReels.signum() < 0 || joursReels.compareTo(demande.getNombreJours()) > 0) {
            throw new CongeDemandeInvalideException("Les jours consommes doivent etre compris entre 0 et " + demande.getNombreJours());
        }
        BigDecimal dejaCompte = demande.getNombreJoursConsomme() == null ? demande.getNombreJours() : demande.getNombreJoursConsomme();
        BigDecimal correction = dejaCompte.subtract(joursReels).setScale(2, RoundingMode.HALF_UP);
        if (correction.signum() == 0) return;
        String reference = "AJUSTEMENT_DEMANDE:" + demande.getId() + ":" + java.util.UUID.randomUUID();
        CongeSolde solde=assurerSolde(demande.getEmploye()); BigDecimal avant=solde.getRestants(); BigDecimal apres=avant.add(correction);
        if (correction.signum() < 0 && apres.compareTo(soldeMinimum()) < 0) {
            throw new CongeDemandeInvalideException("Solde insuffisant pour augmenter la consommation reelle");
        }
        solde.setRestants(apres); solde.setJoursUtilises(solde.getJoursUtilises().subtract(correction)); solde.setDateModification(LocalDateTime.now());
        soldes.save(solde); historiques.enregistrer(solde,demande,"AJUSTEMENT_CONSOMMATION",correction,avant,apres,reference);
    }

    public BigDecimal soldeMinimum() {
        return variables.booleen("solde_negatif", true)
                ? variables.decimal("solde_negatif_max", "-5") : BigDecimal.ZERO;
    }
}
