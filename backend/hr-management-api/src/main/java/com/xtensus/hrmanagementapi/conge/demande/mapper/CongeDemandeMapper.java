package com.xtensus.hrmanagementapi.conge.demande.mapper;

import com.xtensus.hrmanagementapi.conge.demande.dto.CongeDemandeResponse;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import org.springframework.stereotype.Component;

@Component
public class CongeDemandeMapper {
    public CongeDemandeResponse toResponse(CongeDemande demande) {
        CongeDemandeResponse response = new CongeDemandeResponse();
        response.setId(demande.getId());
        response.setEmploye(toEmployeResume(demande.getEmploye()));
        response.setDecideur(toEmployeResume(demande.getDecideur()));
        if (demande.getCongeType() != null) {
            CongeDemandeResponse.TypeCongeResume type = new CongeDemandeResponse.TypeCongeResume();
            type.setId(demande.getCongeType().getId());
            type.setNom(demande.getCongeType().getNom());
            response.setCongeType(type);
        }
        if (demande.getStatut() != null) {
            CongeDemandeResponse.StatutResume statut = new CongeDemandeResponse.StatutResume();
            statut.setId(demande.getStatut().getId());
            statut.setLibelle(demande.getStatut().getLibelle());
            response.setStatut(statut);
        }
        response.setRaison(demande.getRaison() == null ? null : demande.getRaison().getCommentaire());
        response.setRaisonId(demande.getRaison() == null ? null : demande.getRaison().getId());
        response.setCertificatMedicalRequis(estCongeMaladie(demande));
        response.setNature(demande.getNature());
        response.setDateDebut(demande.getDateDebut());
        response.setHeureDebut(demande.getHeureDebut());
        response.setDateFin(demande.getDateFin());
        response.setHeureFin(demande.getHeureFin());
        response.setDateSoumission(demande.getDateSoumission());
        response.setNombreJours(demande.getNombreJours());
        response.setSamediCompte(demande.getSamediCompte());
        response.setNombreJoursConsomme(demande.getNombreJoursConsomme());
        response.setCommentaireEmploye(demande.getCommentaireEmploye());
        response.setCommentaireDecision(demande.getCommentaireDecision());
        response.setDateDecision(demande.getDateDecision());
        return response;
    }

    private CongeDemandeResponse.EmployeResume toEmployeResume(Employe employe) {
        if (employe == null) return null;
        CongeDemandeResponse.EmployeResume resume = new CongeDemandeResponse.EmployeResume();
        resume.setId(employe.getId());
        resume.setNom(employe.getNom());
        resume.setPrenom(employe.getPrenom());
        resume.setEmail(employe.getEmail());
        resume.setPhotoUrl(employe.getPhotoProfil() == null ? null : "/api/employes/" + employe.getId() + "/photo");
        return resume;
    }

    private boolean estCongeMaladie(CongeDemande demande) {
        if (!"CONGE".equals(demande.getNature()) || demande.getRaison() == null || demande.getRaison().getCommentaire() == null) return false;
        String motif = java.text.Normalizer.normalize(demande.getRaison().getCommentaire(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT);
        return motif.contains("malad") || motif.contains("medical") || motif.contains("sante");
    }
}
