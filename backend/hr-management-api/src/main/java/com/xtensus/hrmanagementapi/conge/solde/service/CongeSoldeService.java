package com.xtensus.hrmanagementapi.conge.solde.service;

import com.xtensus.hrmanagementapi.conge.solde.dto.CongeSoldeRequest;
import com.xtensus.hrmanagementapi.conge.solde.dto.CongeSoldeResponse;
import com.xtensus.hrmanagementapi.conge.solde.exception.CongeSoldeExisteDejaException;
import com.xtensus.hrmanagementapi.conge.solde.exception.CongeSoldeIntrouvableException;
import com.xtensus.hrmanagementapi.conge.solde.mapper.CongeSoldeMapper;
import com.xtensus.hrmanagementapi.conge.type.exception.CongeTypeIntrouvableException;
import com.xtensus.hrmanagementapi.domain.entity.CongeSolde;
import com.xtensus.hrmanagementapi.domain.entity.CongeType;
import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.employe.exception.EmployeIntrouvableException;
import com.xtensus.hrmanagementapi.repository.CongeSoldeRepository;
import com.xtensus.hrmanagementapi.repository.CongeTypeRepository;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeSoldeService {
    private final CongeSoldeRepository repo;
    private final EmployeRepository employes;
    private final CongeTypeRepository types;
    private final CongeSoldeMapper mapper;

    public CongeSoldeService(CongeSoldeRepository repo, EmployeRepository employes, CongeTypeRepository types, CongeSoldeMapper mapper) {
        this.repo = repo;
        this.employes = employes;
        this.types = types;
        this.mapper = mapper;
    }

    @Transactional
    public CongeSoldeResponse creer(CongeSoldeRequest req) {
        if (repo.existsByEmployeId(req.getEmployeId())) {
            throw new CongeSoldeExisteDejaException();
        }
        CongeSolde solde = new CongeSolde();
        solde.setEmploye(employe(req.getEmployeId()));
        solde.setCongeType(type(req.getCongeTypeId()));
        mapper.update(req, solde);
        solde.setDateCreation(LocalDateTime.now());
        return mapper.toResponse(repo.save(solde));
    }

    @Transactional
    public CongeSoldeResponse modifier(Long id, CongeSoldeRequest req) {
        CongeSolde solde = entite(id);
        if (repo.existsByEmployeIdAndCongeTypeIdAndAnneeAndIdNot(req.getEmployeId(), req.getCongeTypeId(), req.getAnnee(), id)) {
            throw new CongeSoldeExisteDejaException();
        }
        solde.setEmploye(employe(req.getEmployeId()));
        solde.setCongeType(type(req.getCongeTypeId()));
        mapper.update(req, solde);
        solde.setDateModification(LocalDateTime.now());
        return mapper.toResponse(repo.save(solde));
    }

    @Transactional
    public void supprimer(Long id) {
        repo.delete(entite(id));
    }

    @Transactional(readOnly = true)
    public CongeSoldeResponse trouverParId(Long id) {
        return mapper.toResponse(entite(id));
    }

    @Transactional(readOnly = true)
    public List<CongeSoldeResponse> lister() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeSoldeResponse> parEmploye(Long employeId) {
        return repo.findByEmployeId(employeId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeSoldeResponse> monSolde(Long employeId) { return parEmploye(employeId); }

    @Transactional(readOnly = true)
    public List<CongeSoldeResponse> parAnnee(Integer annee) {
        return repo.findByAnnee(annee).stream().map(mapper::toResponse).toList();
    }

    private CongeSolde entite(Long id) {
        return repo.findById(id).orElseThrow(() -> new CongeSoldeIntrouvableException(id));
    }

    private Employe employe(Long id) {
        return employes.findById(id).orElseThrow(() -> new EmployeIntrouvableException(id));
    }

    private CongeType type(Long id) {
        return types.findById(id).orElseThrow(() -> new CongeTypeIntrouvableException(id));
    }
}
