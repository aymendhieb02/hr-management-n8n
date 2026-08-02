package com.xtensus.hrmanagementapi.raison.service;

import com.xtensus.hrmanagementapi.domain.entity.Raison;
import com.xtensus.hrmanagementapi.raison.dto.RaisonRequest;
import com.xtensus.hrmanagementapi.raison.dto.RaisonResponse;
import com.xtensus.hrmanagementapi.raison.exception.RaisonIntrouvableException;
import com.xtensus.hrmanagementapi.raison.mapper.RaisonMapper;
import com.xtensus.hrmanagementapi.repository.RaisonRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RaisonService {

    private final RaisonRepository repository;
    private final RaisonMapper mapper;

    public RaisonService(RaisonRepository repository, RaisonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public RaisonResponse creer(RaisonRequest request) {
        Raison raison = mapper.toEntity(request);
        raison.setDateCreation(LocalDateTime.now());
        return mapper.toResponse(repository.save(raison));
    }

    @Transactional
    public RaisonResponse modifier(Long id, RaisonRequest request) {
        Raison raison = trouverEntite(id);
        mapper.updateEntity(request, raison);
        return mapper.toResponse(repository.save(raison));
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public RaisonResponse trouverParId(Long id) {
        return mapper.toResponse(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public List<RaisonResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    private Raison trouverEntite(Long id) {
        return repository.findById(id).orElseThrow(() -> new RaisonIntrouvableException(id));
    }
}
