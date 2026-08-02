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
        response.setDateDebut(demande.getDateDebut());
        response.setHeureDebut(demande.getHeureDebut());
        response.setDateFin(demande.getDateFin());
        response.setHeureFin(demande.getHeureFin());
        response.setDateSoumission(demande.getDateSoumission());
        response.setNombreJours(demande.getNombreJours());
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
        return resume;
    }
}
