package com.xtensus.hrmanagementapi.conge.type.service;

import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeRequest;
import com.xtensus.hrmanagementapi.conge.type.dto.CongeTypeResponse;
import com.xtensus.hrmanagementapi.conge.type.exception.CongeTypeExisteDejaException;
import com.xtensus.hrmanagementapi.conge.type.exception.CongeTypeIntrouvableException;
import com.xtensus.hrmanagementapi.conge.type.mapper.CongeTypeMapper;
import com.xtensus.hrmanagementapi.domain.entity.CongeType;
import com.xtensus.hrmanagementapi.repository.CongeTypeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongeTypeService {

    private final CongeTypeRepository congeTypeRepository;
    private final CongeTypeMapper congeTypeMapper;

    public CongeTypeService(CongeTypeRepository congeTypeRepository, CongeTypeMapper congeTypeMapper) {
        this.congeTypeRepository = congeTypeRepository;
        this.congeTypeMapper = congeTypeMapper;
    }

    @Transactional
    public CongeTypeResponse creer(CongeTypeRequest request) {
        String nom = nettoyer(request.getNom());
        verifierUnicite(nom, null);
        CongeType congeType = congeTypeMapper.toEntity(request);
        congeType.setNom(nom);
        congeType.setDateCreation(LocalDateTime.now());
        return congeTypeMapper.toResponse(congeTypeRepository.save(congeType));
    }

    @Transactional
    public CongeTypeResponse modifier(Long id, CongeTypeRequest request) {
        CongeType congeType = trouverEntite(id);
        String nom = nettoyer(request.getNom());
        verifierUnicite(nom, id);
        congeTypeMapper.updateEntity(request, congeType);
        congeType.setNom(nom);
        congeType.setDateModification(LocalDateTime.now());
        return congeTypeMapper.toResponse(congeTypeRepository.save(congeType));
    }

    @Transactional
    public void supprimer(Long id) {
        congeTypeRepository.delete(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public CongeTypeResponse trouverParId(Long id) {
        return congeTypeMapper.toResponse(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public List<CongeTypeResponse> lister() {
        return congeTypeRepository.findAll().stream().map(congeTypeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeTypeResponse> listerActifs() {
        return congeTypeRepository.findByActifTrue().stream().map(congeTypeMapper::toResponse).toList();
    }

    private CongeType trouverEntite(Long id) {
        return congeTypeRepository.findById(id).orElseThrow(() -> new CongeTypeIntrouvableException(id));
    }

    private void verifierUnicite(String nom, Long id) {
        boolean existe = id == null
                ? congeTypeRepository.existsByNomIgnoreCase(nom)
                : congeTypeRepository.existsByNomIgnoreCaseAndIdNot(nom, id);
        if (existe) {
            throw new CongeTypeExisteDejaException(nom);
        }
    }

    private String nettoyer(String valeur) {
        return valeur == null ? null : valeur.trim();
    }
}
