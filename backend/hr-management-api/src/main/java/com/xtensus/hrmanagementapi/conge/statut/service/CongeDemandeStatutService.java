package com.xtensus.hrmanagementapi.conge.statut.service;

import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutRequest;
import com.xtensus.hrmanagementapi.conge.statut.dto.CongeDemandeStatutResponse;
import com.xtensus.hrmanagementapi.conge.statut.exception.CongeDemandeStatutExisteDejaException;
import com.xtensus.hrmanagementapi.conge.statut.exception.CongeDemandeStatutIntrouvableException;
import com.xtensus.hrmanagementapi.conge.statut.mapper.CongeDemandeStatutMapper;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemandeStatut;
import com.xtensus.hrmanagementapi.repository.CongeDemandeStatutRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeDemandeStatutService {

    private final CongeDemandeStatutRepository repository;
    private final CongeDemandeStatutMapper mapper;

    public CongeDemandeStatutService(CongeDemandeStatutRepository repository, CongeDemandeStatutMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public CongeDemandeStatutResponse creer(CongeDemandeStatutRequest request) {
        String libelle = normaliser(request.getLibelle());
        if (repository.existsByLibelleIgnoreCase(libelle)) {
            throw new CongeDemandeStatutExisteDejaException(libelle);
        }
        CongeDemandeStatut statut = mapper.toEntity(request);
        statut.setLibelle(libelle);
        return mapper.toResponse(repository.save(statut));
    }

    @Transactional
    public CongeDemandeStatutResponse modifier(Long id, CongeDemandeStatutRequest request) {
        CongeDemandeStatut statut = trouverEntite(id);
        String libelle = normaliser(request.getLibelle());
        if (repository.existsByLibelleIgnoreCaseAndIdNot(libelle, id)) {
            throw new CongeDemandeStatutExisteDejaException(libelle);
        }
        mapper.updateEntity(request, statut);
        statut.setLibelle(libelle);
        return mapper.toResponse(repository.save(statut));
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public CongeDemandeStatutResponse trouverParId(Long id) {
        return mapper.toResponse(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public List<CongeDemandeStatutResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeDemandeStatutResponse> listerActifs() {
        return repository.findByActifTrue().stream().map(mapper::toResponse).toList();
    }

    private CongeDemandeStatut trouverEntite(Long id) {
        return repository.findById(id).orElseThrow(() -> new CongeDemandeStatutIntrouvableException(id));
    }

    private String normaliser(String value) {
        return value == null ? null : value.trim();
    }
}
