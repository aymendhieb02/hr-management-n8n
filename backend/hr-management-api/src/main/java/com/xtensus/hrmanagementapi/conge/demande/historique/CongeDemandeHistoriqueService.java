package com.xtensus.hrmanagementapi.conge.demande.historique;

import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeHistorique;
import com.xtensus.hrmanagementapi.repository.CongeDemandeHistoriqueRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
        return repository.findAllByOrderByDateActionDesc().stream()
                .filter(h -> !"BROUILLON".equals(h.getDemande().getStatut().getLibelle()))
                .map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeDemandeHistoriqueResponse> listerParEmploye(Long employeId) {
        return repository.rechercher(null, employeId, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "dateAction")))
                .getContent().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public CongeDemandeHistoriquePageResponse rechercher(String recherche, Long employeId, int page, int taille) {
        String terme = recherche == null || recherche.isBlank() ? null : recherche.trim();
        var resultat = repository.rechercher(terme, employeId,
                PageRequest.of(page, taille, Sort.by(Sort.Direction.DESC, "dateAction")));
        return CongeDemandeHistoriquePageResponse.builder()
                .contenu(resultat.getContent().stream().map(this::response).toList())
                .totalElements(resultat.getTotalElements()).totalPages(resultat.getTotalPages())
                .page(resultat.getNumber()).taille(resultat.getSize()).build();
    }

    @Transactional(readOnly = true)
    public List<CongeDemandeHistoriqueEmployeResponse> listerEmployes() {
        return repository.listerEmployesAvecHistorique().stream()
                .map(row -> new CongeDemandeHistoriqueEmployeResponse(((Number) row[0]).longValue(), (String) row[1]))
                .toList();
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
