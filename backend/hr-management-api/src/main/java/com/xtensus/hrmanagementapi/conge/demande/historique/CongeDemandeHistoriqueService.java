package com.xtensus.hrmanagementapi.conge.demande.historique;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeHistorique;
import com.xtensus.hrmanagementapi.repository.CongeDemandeHistoriqueRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeDemandeHistoriqueService {
    private final CongeDemandeHistoriqueRepository repository;
    public CongeDemandeHistoriqueService(CongeDemandeHistoriqueRepository repository) { this.repository = repository; }

    public void enregistrer(CongeDemande demande, String action, String ancien, String nouveau, String commentaire) {
        CongeDemandeHistorique h = new CongeDemandeHistorique();
        h.setDemande(demande); h.setDateAction(LocalDateTime.now()); h.setAction(action);
        h.setAncienStatut(ancien); h.setNouveauStatut(nouveau); h.setCommentaire(commentaire);
        repository.save(h);
    }

    @Transactional(readOnly = true)
    public List<CongeDemandeHistoriqueResponse> lister() {
        return repository.findAllByOrderByDateActionDesc().stream().map(this::response).toList();
    }

    @Transactional
    public CongeDemandeHistoriqueResponse modifier(Long id, CongeDemandeHistoriqueRequest request) {
        CongeDemandeHistorique h = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Historique introuvable"));
        h.setAction(request.getAction().trim()); h.setCommentaire(request.getCommentaire());
        h.setAncienStatut(request.getAncienStatut()); h.setNouveauStatut(request.getNouveauStatut());
        return response(repository.save(h));
    }

    @Transactional public void supprimer(Long id) { repository.deleteById(id); }

    private CongeDemandeHistoriqueResponse response(CongeDemandeHistorique h) {
        CongeDemande d = h.getDemande();
        return CongeDemandeHistoriqueResponse.builder().id(h.getId()).demandeId(d.getId())
                .dateAction(h.getDateAction()).action(h.getAction()).commentaire(h.getCommentaire())
                .ancienStatut(h.getAncienStatut()).nouveauStatut(h.getNouveauStatut())
                .employeId(d.getEmploye().getId())
                .employeNomComplet(d.getEmploye().getPrenom() + " " + d.getEmploye().getNom())
                .typeDemande(d.getNature()).typeConge(d.getCongeType().getNom())
                .dateDebut(d.getDateDebut()).dateFin(d.getDateFin()).heureDebut(d.getHeureDebut()).heureFin(d.getHeureFin())
                .statutActuel(d.getStatut().getLibelle()).build();
    }
}
