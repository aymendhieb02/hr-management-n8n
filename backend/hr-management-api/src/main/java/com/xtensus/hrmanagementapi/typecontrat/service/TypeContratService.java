package com.xtensus.hrmanagementapi.typecontrat.service;

import com.xtensus.hrmanagementapi.domain.entity.TypeContrat;
import com.xtensus.hrmanagementapi.repository.TypeContratRepository;
import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratRequest;
import com.xtensus.hrmanagementapi.typecontrat.dto.TypeContratResponse;
import com.xtensus.hrmanagementapi.typecontrat.exception.TypeContratExisteDejaException;
import com.xtensus.hrmanagementapi.typecontrat.exception.TypeContratIntrouvableException;
import com.xtensus.hrmanagementapi.typecontrat.mapper.TypeContratMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TypeContratService {

    private final TypeContratRepository typeContratRepository;
    private final TypeContratMapper typeContratMapper;

    public TypeContratService(TypeContratRepository typeContratRepository, TypeContratMapper typeContratMapper) {
        this.typeContratRepository = typeContratRepository;
        this.typeContratMapper = typeContratMapper;
    }

    @Transactional
    public TypeContratResponse creer(TypeContratRequest request) {
        String libelle = nettoyer(request.getLibelle());
        verifierUnicite(libelle, null);
        TypeContrat typeContrat = typeContratMapper.toEntity(request);
        typeContrat.setLibelle(libelle);
        typeContrat.setDateCreation(LocalDateTime.now());
        return typeContratMapper.toResponse(typeContratRepository.save(typeContrat));
    }

    @Transactional
    public TypeContratResponse modifier(Long id, TypeContratRequest request) {
        TypeContrat typeContrat = trouverEntite(id);
        String libelle = nettoyer(request.getLibelle());
        verifierUnicite(libelle, id);
        typeContratMapper.updateEntity(request, typeContrat);
        typeContrat.setLibelle(libelle);
        // Les anciens exports MySQL pouvaient contenir 0000-00-00, converti en
        // null par JDBC. Réparer cette valeur avant l'UPDATE d'une colonne NOT NULL.
        if (typeContrat.getDateCreation() == null) {
            typeContrat.setDateCreation(LocalDateTime.now());
        }
        typeContrat.setDateModification(LocalDateTime.now());
        return typeContratMapper.toResponse(typeContratRepository.save(typeContrat));
    }

    @Transactional
    public void supprimer(Long id) {
        typeContratRepository.delete(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public TypeContratResponse trouverParId(Long id) {
        return typeContratMapper.toResponse(trouverEntite(id));
    }

    @Transactional(readOnly = true)
    public List<TypeContratResponse> lister() {
        return typeContratRepository.findAll().stream().map(typeContratMapper::toResponse).toList();
    }

    private TypeContrat trouverEntite(Long id) {
        return typeContratRepository.findById(id).orElseThrow(() -> new TypeContratIntrouvableException(id));
    }

    private void verifierUnicite(String libelle, Long id) {
        boolean existe = id == null
                ? typeContratRepository.existsByLibelleIgnoreCase(libelle)
                : typeContratRepository.existsByLibelleIgnoreCaseAndIdNot(libelle, id);
        if (existe) {
            throw new TypeContratExisteDejaException(libelle);
        }
    }

    private String nettoyer(String valeur) {
        return valeur == null ? null : valeur.trim();
    }
}
