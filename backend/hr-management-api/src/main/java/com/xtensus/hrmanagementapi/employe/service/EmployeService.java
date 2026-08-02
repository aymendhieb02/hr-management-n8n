package com.xtensus.hrmanagementapi.employe.service;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.entity.Poste;
import com.xtensus.hrmanagementapi.domain.entity.TypeContrat;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.employe.dto.EmployeRequest;
import com.xtensus.hrmanagementapi.employe.dto.EmployeResponse;
import com.xtensus.hrmanagementapi.employe.exception.EmployeExisteDejaException;
import com.xtensus.hrmanagementapi.employe.exception.EmployeIntrouvableException;
import com.xtensus.hrmanagementapi.employe.exception.EmployeInvalideException;
import com.xtensus.hrmanagementapi.employe.mapper.EmployeMapper;
import com.xtensus.hrmanagementapi.poste.exception.PosteIntrouvableException;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import com.xtensus.hrmanagementapi.repository.PosteRepository;
import com.xtensus.hrmanagementapi.repository.TypeContratRepository;
import com.xtensus.hrmanagementapi.typecontrat.exception.TypeContratIntrouvableException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final PosteRepository posteRepository;
    private final TypeContratRepository typeContratRepository;
    private final EmployeMapper employeMapper;

    public EmployeService(
            EmployeRepository employeRepository,
            PosteRepository posteRepository,
            TypeContratRepository typeContratRepository,
            EmployeMapper employeMapper
    ) {
        this.employeRepository = employeRepository;
        this.posteRepository = posteRepository;
        this.typeContratRepository = typeContratRepository;
        this.employeMapper = employeMapper;
    }

    @Transactional
    public EmployeResponse create(EmployeRequest request) {
        String email = normalizeRequired(request.getEmail(), "L'email est obligatoire");
        if (employeRepository.existsByEmailIgnoreCase(email)) {
            throw new EmployeExisteDejaException(email);
        }

        Employe employe = employeMapper.toEntity(request);
        employe.setEmail(email);
        applyTechnicalDefaults(employe);
        attachReferences(request, employe);
        employe.setCreatedAt(LocalDateTime.now());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Transactional
    public EmployeResponse update(Long id, EmployeRequest request) {
        Employe employe = findEntity(id);
        String email = normalizeRequired(request.getEmail(), "L'email est obligatoire");
        if (employeRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new EmployeExisteDejaException(email);
        }

        employeMapper.updateEntity(request, employe);
        employe.setEmail(email);
        applyTechnicalDefaults(employe);
        attachReferences(request, employe);
        if (employe.getManager() != null && employe.getManager().getId().equals(employe.getId())) {
            throw new EmployeInvalideException("Un employe ne peut pas etre son propre manager");
        }
        employe.setUpdatedAt(LocalDateTime.now());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Transactional
    public void delete(Long id) {
        Employe employe = findEntity(id);
        employeRepository.delete(employe);
    }

    @Transactional(readOnly = true)
    public EmployeResponse findById(Long id) {
        return employeMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<EmployeResponse> findAll() {
        return employeRepository.findAll().stream().map(employeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeResponse> findActifs() {
        return employeRepository.findByActifTrue().stream().map(employeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeResponse> findEquipe(Long managerId) {
        return employeRepository.findByManagerId(managerId).stream().map(employeMapper::toResponse).toList();
    }

    private void applyTechnicalDefaults(Employe employe) {
        employe.setUsername(employe.getEmail());
        if (employe.getMotDePasseHash() == null) {
            employe.setMotDePasseHash("NON_AUTHENTIFICATION");
        }
        if (employe.getRole() == null || employe.getRole().isBlank()) {
            employe.setRole(RoleType.EMPLOYEE.toDatabaseRole());
        }
        employe.setStatut(Boolean.TRUE.equals(employe.getActif()) ? "ACTIF" : "INACTIF");
    }
    private Employe findEntity(Long id) {
        return employeRepository.findById(id).orElseThrow(() -> new EmployeIntrouvableException(id));
    }

    private void attachReferences(EmployeRequest request, Employe employe) {
        employe.setPoste(findPoste(request.getPosteId()));
        employe.setTypeContrat(findTypeContrat(request.getTypeContratId()));
        employe.setManager(findManager(request.getManagerId()));
    }

    private Poste findPoste(Long id) {
        if (id == null) {
            return null;
        }
        return posteRepository.findById(id).orElseThrow(() -> new PosteIntrouvableException(id));
    }

    private TypeContrat findTypeContrat(Long id) {
        if (id == null) {
            return null;
        }
        return typeContratRepository.findById(id).orElseThrow(() -> new TypeContratIntrouvableException(id));
    }

    private Employe findManager(Long id) {
        if (id == null) {
            return null;
        }
        return employeRepository.findById(id).orElseThrow(() -> new EmployeIntrouvableException(id));
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmployeInvalideException(message);
        }
        return value.trim();
    }
}



