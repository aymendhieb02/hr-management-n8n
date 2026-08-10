package com.xtensus.hrmanagementapi.jourferie.service;

import com.xtensus.hrmanagementapi.domain.entity.JourFerie;
import com.xtensus.hrmanagementapi.jourferie.dto.JourFerieRequest;
import com.xtensus.hrmanagementapi.jourferie.dto.JourFerieResponse;
import com.xtensus.hrmanagementapi.repository.JourFerieRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JourFerieService {

    private final JourFerieRepository repository;

    public JourFerieService(JourFerieRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JourFerieResponse creer(JourFerieRequest request) {
        if (repository.findByDate(request.getDate()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un jour férié existe déjà pour la date " + request.getDate());
        }
        JourFerie entity = new JourFerie();
        entity.setNom(request.getNom());
        entity.setDate(request.getDate());
        entity.setDescription(request.getDescription());
        entity.setActif(request.getActif() != null ? request.getActif() : true);
        entity.setDateCreation(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public JourFerieResponse modifier(Long id, JourFerieRequest request) {
        JourFerie entity = trouverEntite(id);
        if (repository.existsByDateAndIdNot(request.getDate(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un jour férié existe déjà pour la date " + request.getDate());
        }
        entity.setNom(request.getNom());
        entity.setDate(request.getDate());
        entity.setDescription(request.getDescription());
        if (request.getActif() != null) {
            entity.setActif(request.getActif());
        }
        entity.setDateModification(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void supprimer(Long id) {
        JourFerie entity = trouverEntite(id);
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public JourFerieResponse trouverParId(Long id) {
        return toResponse(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public List<JourFerieResponse> lister() {
        return repository.findAllByOrderByDateAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<JourFerieResponse> listerActifs() {
        return repository.findByActifTrueOrderByDateAsc().stream().map(this::toResponse).toList();
    }

    private JourFerie trouverEntite(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jour férié introuvable avec l'id " + id));
    }

    private JourFerieResponse toResponse(JourFerie entity) {
        return JourFerieResponse.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .date(entity.getDate())
                .description(entity.getDescription())
                .actif(entity.getActif())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .build();
    }
}
