package com.xtensus.hrmanagementapi.poste.service;

import com.xtensus.hrmanagementapi.domain.entity.Poste;
import com.xtensus.hrmanagementapi.poste.dto.PosteRequest;
import com.xtensus.hrmanagementapi.poste.dto.PosteResponse;
import com.xtensus.hrmanagementapi.poste.exception.PosteExisteDejaException;
import com.xtensus.hrmanagementapi.poste.exception.PosteIntrouvableException;
import com.xtensus.hrmanagementapi.poste.mapper.PosteMapper;
import com.xtensus.hrmanagementapi.repository.PosteRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PosteService {

    private final PosteRepository posteRepository;
    private final PosteMapper posteMapper;

    public PosteService(PosteRepository posteRepository, PosteMapper posteMapper) {
        this.posteRepository = posteRepository;
        this.posteMapper = posteMapper;
    }

    @Transactional
    public PosteResponse creer(PosteRequest request) {
        String intitule = nettoyer(request.getIntitule());
        verifierUnicite(intitule, null);
        Poste poste = posteMapper.toEntity(request);
        poste.setIntitule(intitule);
        poste.setDateCreation(LocalDateTime.now());
        return posteMapper.toResponse(posteRepository.save(poste));
    }

    @Transactional
    public PosteResponse modifier(Long id, PosteRequest request) {
        Poste poste = trouverEntite(id);
        String intitule = nettoyer(request.getIntitule());
        verifierUnicite(intitule, id);
        posteMapper.updateEntity(request, poste);
        poste.setIntitule(intitule);
        poste.setDateModification(LocalDateTime.now());
        return posteMapper.toResponse(posteRepository.save(poste));
    }

    @Transactional
    public void supprimer(Long id) {
        posteRepository.delete(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public PosteResponse trouverParId(Long id) {
        return posteMapper.toResponse(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public List<PosteResponse> lister() {
        return posteRepository.findAll().stream().map(posteMapper::toResponse).toList();
    }

    private Poste trouverEntite(Long id) {
        return posteRepository.findById(id).orElseThrow(() -> new PosteIntrouvableException(id));
    }

    private void verifierUnicite(String intitule, Long id) {
        boolean existe = id == null
                ? posteRepository.existsByIntituleIgnoreCase(intitule)
                : posteRepository.existsByIntituleIgnoreCaseAndIdNot(intitule, id);
        if (existe) {
            throw new PosteExisteDejaException(intitule);
        }
    }

    private String nettoyer(String valeur) {
        return valeur == null ? null : valeur.trim();
    }
}
