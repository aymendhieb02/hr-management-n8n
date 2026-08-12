package com.xtensus.hrmanagementapi.conge.solde.historique;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.CongeSolde;
import com.xtensus.hrmanagementapi.domain.entity.CongeSoldeHistorique;
import com.xtensus.hrmanagementapi.repository.CongeSoldeHistoriqueRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeSoldeHistoriqueService {
    private final CongeSoldeHistoriqueRepository repository;
    public CongeSoldeHistoriqueService(CongeSoldeHistoriqueRepository repository) { this.repository = repository; }

    @Transactional
    public void enregistrer(CongeSolde solde, CongeDemande demande, String type, BigDecimal montant,
            BigDecimal avant, BigDecimal apres, String reference) {
        CongeSoldeHistorique historique = new CongeSoldeHistorique();
        historique.setSolde(solde); historique.setDemande(demande); historique.setTypeTransaction(type);
        historique.setMontant(montant); historique.setSoldeAvant(avant); historique.setSoldeApres(apres);
        historique.setReference(reference); historique.setDateExecution(LocalDateTime.now());
        historique.setStatutAcquisition("SUCCES");
        repository.save(historique);
    }

    @Transactional(readOnly = true)
    public List<CongeSoldeHistoriqueResponse> parEmploye(Long id) {
        return repository.findBySoldeEmployeIdOrderByDateExecutionDesc(id).stream().map(this::map).toList();
    }
    @Transactional(readOnly = true)
    public List<CongeSoldeHistoriqueResponse> tous() {
        return repository.findAllByOrderByDateExecutionDesc().stream().map(this::map).toList();
    }
    public boolean existe(String reference) { return repository.existsByReference(reference); }
    private CongeSoldeHistoriqueResponse map(CongeSoldeHistorique h) {
        CongeSoldeHistoriqueResponse r = new CongeSoldeHistoriqueResponse();
        r.setId(h.getId()); r.setSoldeId(h.getSolde().getId()); r.setEmployeId(h.getSolde().getEmploye().getId());
        r.setEmployeNomComplet(h.getSolde().getEmploye().getPrenom()+" "+h.getSolde().getEmploye().getNom());
        r.setDemandeId(h.getDemande()==null?null:h.getDemande().getId()); r.setTypeTransaction(h.getTypeTransaction());
        r.setMontant(h.getMontant()); r.setSoldeAvant(h.getSoldeAvant()); r.setSoldeApres(h.getSoldeApres());
        r.setDateExecution(h.getDateExecution()); r.setStatut(h.getStatutAcquisition()); r.setErreur(h.getErreur()); return r;
    }
}
