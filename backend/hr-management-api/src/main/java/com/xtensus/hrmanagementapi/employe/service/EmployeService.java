package com.xtensus.hrmanagementapi.employe.service;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.entity.Poste;
import com.xtensus.hrmanagementapi.domain.entity.TypeContrat;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.employe.dto.EmployeRequest;
import com.xtensus.hrmanagementapi.employe.dto.EmployeResponse;
import com.xtensus.hrmanagementapi.employe.dto.MotDePasseModificationRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final PosteRepository posteRepository;
    private final TypeContratRepository typeContratRepository;
    private final EmployeMapper employeMapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeService(
            EmployeRepository employeRepository,
            PosteRepository posteRepository,
            TypeContratRepository typeContratRepository,
            EmployeMapper employeMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.employeRepository = employeRepository;
        this.posteRepository = posteRepository;
        this.typeContratRepository = typeContratRepository;
        this.employeMapper = employeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeResponse create(EmployeRequest request) {
        return createInternal(request, null);
    }

    @Transactional
    public EmployeResponse createForManager(EmployeRequest request, Long managerId) {
        return createInternal(request, findEntity(managerId));
    }

    private EmployeResponse createInternal(EmployeRequest request, Employe forcedManager) {
        String email = normalizeRequired(request.getEmail(), "L'email est obligatoire");
        String username = normalizeRequired(request.getUsername(), "L'identifiant est obligatoire");
        if (employeRepository.existsByEmailIgnoreCase(email)) {
            throw new EmployeExisteDejaException(email);
        }
        if (employeRepository.existsByUsernameIgnoreCase(username)) {
            throw new EmployeExisteDejaException(username);
        }

        Employe employe = employeMapper.toEntity(request);
        employe.setEmail(email);
        employe.setUsername(username);
        applyTechnicalDefaults(employe);
        attachReferences(request, employe);
        if (forcedManager != null) {
            employe.setManager(forcedManager);
            employe.setRole(RoleType.EMPLOYEE.toDatabaseRole());
        }
        employe.setCreatedAt(LocalDateTime.now());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Transactional
    public EmployeResponse update(Long id, EmployeRequest request) {
        Employe employe = findEntity(id);
        String email = normalizeRequired(request.getEmail(), "L'email est obligatoire");
        String username = normalizeRequired(request.getUsername(), "L'identifiant est obligatoire");
        if (employeRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new EmployeExisteDejaException(email);
        }
        if (employeRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new EmployeExisteDejaException(username);
        }

        employeMapper.updateEntity(request, employe);
        employe.setEmail(email);
        employe.setUsername(username);
        applyTechnicalDefaults(employe);
        attachReferences(request, employe);
        if (employe.getManager() != null && employe.getManager().getId().equals(employe.getId())) {
            throw new EmployeInvalideException("Un employe ne peut pas etre son propre manager");
        }
        employe.setUpdatedAt(LocalDateTime.now());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Transactional
    public EmployeResponse updateForManager(Long id, EmployeRequest request, Long managerId) {
        ensureManagedBy(id, managerId);
        EmployeResponse response = update(id, request);
        Employe employe = findEntity(id);
        employe.setManager(findEntity(managerId));
        employe.setRole(RoleType.EMPLOYEE.toDatabaseRole());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Transactional
    public void delete(Long id) {
        Employe employe = findEntity(id);
        employeRepository.delete(employe);
    }

    @Transactional
    public void deleteForManager(Long id, Long managerId) {
        ensureManagedBy(id, managerId);
        delete(id);
    }

    @Transactional
    public EmployeResponse setActiveForManager(Long id, boolean active, Long managerId) {
        ensureManagedBy(id, managerId);
        return setActive(id, active);
    }

    @Transactional
    public EmployeResponse setActive(Long id, boolean active) {
        Employe employe = findEntity(id);
        employe.setActif(active);
        employe.setStatut(active ? "ACTIF" : "INACTIF");
        employe.setUpdatedAt(LocalDateTime.now());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    @Transactional
    public void resetPasswordToDefault(Long id) {
        findEntity(id);
        employeRepository.resetPasswordToDatabaseDefault(id);
    }

    @Transactional
    public void resetPasswordToDefaultForManager(Long id, Long managerId) {
        ensureManagedBy(id, managerId);
        employeRepository.resetPasswordToDatabaseDefault(id);
    }

    @Transactional(readOnly = true)
    public EmployeResponse findAccessible(Long id, Long actorId, boolean manager) {
        if (id.equals(actorId)) return findById(id);
        if (manager) ensureManagedBy(id, actorId);
        return findById(id);
    }

    @Transactional
    public void updateOwnPassword(Long employeId, MotDePasseModificationRequest request) {
        Employe employe = findEntity(employeId);
        if (!passwordEncoder.matches(request.getMotDePasseActuel(), employe.getMotDePasseHash())) {
            throw new EmployeInvalideException("Le mot de passe actuel est incorrect");
        }
        employe.setMotDePasseHash(passwordEncoder.encode(request.getNouveauMotDePasse()));
        employe.setUpdatedAt(LocalDateTime.now());
        employeRepository.save(employe);
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
        if (employe.getRole() == null || employe.getRole().isBlank()) {
            employe.setRole(RoleType.EMPLOYEE.toDatabaseRole());
        }
        employe.setStatut(Boolean.TRUE.equals(employe.getActif()) ? "ACTIF" : "INACTIF");
    }
    private Employe findEntity(Long id) {
        return employeRepository.findById(id).orElseThrow(() -> new EmployeIntrouvableException(id));
    }

    private void ensureManagedBy(Long employeId, Long managerId) {
        Employe employe = findEntity(employeId);
        if (employe.getManager() == null || !managerId.equals(employe.getManager().getId())) {
            throw new EmployeInvalideException("Ce manager ne peut gerer que les membres de son equipe");
        }
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



