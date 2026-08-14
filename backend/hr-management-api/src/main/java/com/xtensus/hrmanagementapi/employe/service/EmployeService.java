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
import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.xtensus.hrmanagementapi.notificationfr.service.NotificationFrancaiseService;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final PosteRepository posteRepository;
    private final TypeContratRepository typeContratRepository;
    private final EmployeMapper employeMapper;
    private final PasswordEncoder passwordEncoder;
    private final CompteEmailService compteEmailService;
    private final NotificationFrancaiseService notificationService;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";

    public EmployeService(
            EmployeRepository employeRepository,
            PosteRepository posteRepository,
            TypeContratRepository typeContratRepository,
            EmployeMapper employeMapper,
            PasswordEncoder passwordEncoder,
            CompteEmailService compteEmailService,
            NotificationFrancaiseService notificationService
    ) {
        this.employeRepository = employeRepository;
        this.posteRepository = posteRepository;
        this.typeContratRepository = typeContratRepository;
        this.employeMapper = employeMapper;
        this.passwordEncoder = passwordEncoder;
        this.compteEmailService = compteEmailService;
        this.notificationService = notificationService;
    }

    @Transactional
    public EmployeResponse create(EmployeRequest request) {
        return createInternal(request, null);
    }

    @Transactional
    public EmployeResponse createForManager(EmployeRequest request, Long managerId) {
        return createInternal(request, findEntity(managerId));
    }

    @Transactional
    public EmployeResponse createForHr(EmployeRequest request) {
        request.setRole(RoleType.EMPLOYEE.toDatabaseRole());
        return createInternal(request, null);
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
        validerRoleUnique(employe.getRole(), null);
        attachReferences(request, employe);
        if (forcedManager != null) {
            employe.setManager(forcedManager);
            employe.setRole(RoleType.EMPLOYEE.toDatabaseRole());
        }
        employe.setCreatedAt(LocalDateTime.now());
        String secret = genererSecretTemporaire();
        employe.setMotDePasseHash(passwordEncoder.encode(secret));
        employe.setCodeActivationHash(passwordEncoder.encode(secret));
        employe.setChangementMotDePasseRequis(true);
        employe.setCodeActivationExpireLe(LocalDateTime.now().plus(7, ChronoUnit.DAYS));
        Employe saved = employeRepository.save(employe);
        notificationService.notifier(saved, "SECURITE_COMPTE", "Changez votre mot de passe temporaire",
                "Pour sécuriser votre compte, modifiez le mot de passe temporaire reçu par email dès votre première connexion.", "HAUTE");
        compteEmailService.envoyerBienvenue(saved, secret);
        return employeMapper.toResponse(saved);
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
        validerRoleUnique(employe.getRole(), id);
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
    public EmployeResponse updateForHr(Long id, EmployeRequest request) {
        request.setRole(findEntity(id).getRole());
        return update(id, request);
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
        reinitialiserMotDePasse(findEntity(id));
    }

    @Transactional
    public void resetPasswordToDefaultForManager(Long id, Long managerId) {
        ensureManagedBy(id, managerId);
        reinitialiserMotDePasse(findEntity(id));
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
        employe.setCodeActivationHash(null);
        employe.setCodeActivationExpireLe(null);
        employe.setChangementMotDePasseRequis(false);
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

    @Transactional
    public EmployeResponse attribuerRole(Long id, String roleDemande, RoleType roleActeur, Long acteurId) {
        Employe employe = findEntity(id);
        RoleType nouveauRole;
        try { nouveauRole = RoleType.valueOf(roleDemande == null ? "" : roleDemande.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new EmployeInvalideException("Role invalide"); }
        if ((roleActeur == RoleType.DG || roleActeur == RoleType.DT)) {
            ensureManagedBy(id, acteurId);
            if (nouveauRole == RoleType.ADMIN) throw new EmployeInvalideException("Seul un administrateur peut attribuer le role Administrateur");
        } else if (roleActeur != RoleType.ADMIN) {
            throw new EmployeInvalideException("Vous n'etes pas autorise a attribuer un role");
        }
        validerRoleUnique(nouveauRole.toDatabaseRole(), id);
        employe.setRole(nouveauRole.toDatabaseRole());
        employe.setUpdatedAt(LocalDateTime.now());
        return employeMapper.toResponse(employeRepository.save(employe));
    }

    private void validerRoleUnique(String role, Long employeId) {
        RoleType type = RoleType.fromDatabaseRole(role);
        if ((type == RoleType.DG || type == RoleType.DT)
                && employeRepository.existsByRoleIgnoreCaseAndIdNot(type.toDatabaseRole(), employeId == null ? -1L : employeId)) {
            throw new EmployeInvalideException(type == RoleType.DG
                    ? "Un directeur general est deja designe."
                    : "Un directeur technique est deja designe.");
        }
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

    private void reinitialiserMotDePasse(Employe employe) {
        String secret = genererSecretTemporaire();
        employe.setMotDePasseHash(passwordEncoder.encode(secret));
        employe.setCodeActivationHash(passwordEncoder.encode(secret));
        employe.setChangementMotDePasseRequis(true);
        employe.setCodeActivationExpireLe(LocalDateTime.now().plusDays(7));
        employe.setUpdatedAt(LocalDateTime.now());
        employeRepository.save(employe);
        compteEmailService.envoyerBienvenue(employe, secret);
    }

    private String genererSecretTemporaire() {
        StringBuilder value = new StringBuilder(14);
        for (int i = 0; i < 14; i++) value.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return value.toString();
    }
}



