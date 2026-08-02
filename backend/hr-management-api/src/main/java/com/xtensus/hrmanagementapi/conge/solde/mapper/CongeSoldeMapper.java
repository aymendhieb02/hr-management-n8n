package com.xtensus.hrmanagementapi.conge.solde.mapper;

import com.xtensus.hrmanagementapi.conge.solde.dto.*; import com.xtensus.hrmanagementapi.domain.entity.*; import org.springframework.stereotype.Component;
@Component
public class CongeSoldeMapper {
    public CongeSoldeResponse toResponse(CongeSolde s) { CongeSoldeResponse r = new CongeSoldeResponse(); r.setId(s.getId()); r.setAnnee(s.getAnnee()); r.setDroitAcquis(s.getDroitAcquis()); r.setJoursUtilises(s.getJoursUtilises()); r.setRestants(s.getRestants()); r.setDateCreation(s.getDateCreation()); r.setDateModification(s.getDateModification()); if (s.getEmploye()!=null){r.setEmployeId(s.getEmploye().getId()); r.setEmployeNom(s.getEmploye().getNom()); r.setEmployePrenom(s.getEmploye().getPrenom());} if(s.getCongeType()!=null){r.setCongeTypeId(s.getCongeType().getId()); r.setCongeTypeNom(s.getCongeType().getNom());} return r; }
    public void update(CongeSoldeRequest req, CongeSolde s) { s.setAnnee(req.getAnnee()); s.setDroitAcquis(req.getDroitAcquis()); s.setJoursUtilises(req.getJoursUtilises()); s.setRestants(req.getRestants()); }
}
