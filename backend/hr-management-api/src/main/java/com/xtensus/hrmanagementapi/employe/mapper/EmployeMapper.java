package com.xtensus.hrmanagementapi.employe.mapper;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.entity.Poste;
import com.xtensus.hrmanagementapi.domain.entity.TypeContrat;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.employe.dto.EmployeRequest;
import com.xtensus.hrmanagementapi.employe.dto.EmployeResponse;
import org.springframework.stereotype.Component;

@Component
public class EmployeMapper {

    public Employe toEntity(EmployeRequest request) {
        Employe employe = new Employe();
        updateSimpleFields(request, employe);
        return employe;
    }

    public EmployeResponse toResponse(Employe employe) {
        EmployeResponse response = new EmployeResponse();
        response.setId(employe.getId());
        response.setUsername(employe.getUsername());
        response.setNom(employe.getNom());
        response.setPrenom(employe.getPrenom());
        response.setEmail(employe.getEmail());
        response.setTelephone(employe.getTelephone());
        response.setAdresse(employe.getAdresse());
        response.setDateNaissance(employe.getDateNaissance());
        response.setDateEmbauche(employe.getDateEmbauche());
        response.setSexe(employe.getSexe());
        response.setActif(employe.getActif());
        response.setRole(RoleType.fromDatabaseRole(employe.getRole()).name());
        response.setStatut(employe.getStatut());
        response.setPoste(toPosteResume(employe.getPoste()));
        response.setTypeContrat(toTypeContratResume(employe.getTypeContrat()));
        response.setManager(toManagerResume(employe.getManager()));
        response.setCreatedAt(employe.getCreatedAt());
        response.setUpdatedAt(employe.getUpdatedAt());
        return response;
    }

    public void updateEntity(EmployeRequest request, Employe employe) {
        updateSimpleFields(request, employe);
    }

    private void updateSimpleFields(EmployeRequest request, Employe employe) {
        employe.setUsername(trim(request.getUsername()));
        employe.setNom(trim(request.getNom()));
        employe.setPrenom(trim(request.getPrenom()));
        employe.setEmail(trim(request.getEmail()));
        employe.setTelephone(trim(request.getTelephone()));
        employe.setAdresse(trim(request.getAdresse()));
        employe.setDateNaissance(request.getDateNaissance());
        employe.setDateEmbauche(request.getDateEmbauche());
        employe.setSexe(trim(request.getSexe()));
        employe.setActif(request.getActif());
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            employe.setRole(RoleType.valueOf(request.getRole().trim().toUpperCase()).toDatabaseRole());
        }
    }

    private EmployeResponse.PosteResume toPosteResume(Poste poste) {
        if (poste == null) {
            return null;
        }
        EmployeResponse.PosteResume resume = new EmployeResponse.PosteResume();
        resume.setId(poste.getId());
        resume.setIntitule(poste.getIntitule());
        return resume;
    }

    private EmployeResponse.TypeContratResume toTypeContratResume(TypeContrat typeContrat) {
        if (typeContrat == null) {
            return null;
        }
        EmployeResponse.TypeContratResume resume = new EmployeResponse.TypeContratResume();
        resume.setId(typeContrat.getId());
        resume.setLibelle(typeContrat.getLibelle());
        return resume;
    }

    private EmployeResponse.ManagerResume toManagerResume(Employe manager) {
        if (manager == null) {
            return null;
        }
        EmployeResponse.ManagerResume resume = new EmployeResponse.ManagerResume();
        resume.setId(manager.getId());
        resume.setNom(manager.getNom());
        resume.setPrenom(manager.getPrenom());
        resume.setEmail(manager.getEmail());
        return resume;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}


