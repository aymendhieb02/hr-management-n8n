package com.xtensus.hrmanagementapi.notificationfr.service;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.domain.entity.NotificationFrancaise;
import com.xtensus.hrmanagementapi.domain.entity.NotificationType;
import com.xtensus.hrmanagementapi.employe.exception.EmployeIntrouvableException;
import com.xtensus.hrmanagementapi.notificationfr.dto.NotificationFrancaiseRequest;
import com.xtensus.hrmanagementapi.notificationfr.dto.NotificationFrancaiseResponse;
import com.xtensus.hrmanagementapi.notificationfr.exception.NotificationFrancaiseIntrouvableException;
import com.xtensus.hrmanagementapi.notificationfr.exception.NotificationTypeIntrouvableException;
import com.xtensus.hrmanagementapi.notificationfr.mapper.NotificationFrancaiseMapper;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import com.xtensus.hrmanagementapi.repository.NotificationFrancaiseRepository;
import com.xtensus.hrmanagementapi.repository.NotificationTypeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationFrancaiseService {
    private final NotificationFrancaiseRepository repository;
    private final NotificationTypeRepository types;
    private final EmployeRepository employes;
    private final NotificationFrancaiseMapper mapper;

    public NotificationFrancaiseService(NotificationFrancaiseRepository repository, NotificationTypeRepository types,
            EmployeRepository employes, NotificationFrancaiseMapper mapper) {
        this.repository = repository;
        this.types = types;
        this.employes = employes;
        this.mapper = mapper;
    }

    @Transactional
    public NotificationFrancaiseResponse creer(NotificationFrancaiseRequest request) {
        Employe employe = employes.findById(request.getEmployeId()).orElseThrow(() -> new EmployeIntrouvableException(request.getEmployeId()));
        NotificationType type = types.findById(request.getTypeId()).orElseThrow(() -> new NotificationTypeIntrouvableException(request.getTypeId()));
        NotificationFrancaise notification = new NotificationFrancaise();
        notification.setEmploye(employe);
        notification.setType(type);
        notification.setTitre(request.getTitre().trim());
        notification.setContenu(request.getContenu().trim());
        notification.setPriorite(request.getPriorite() == null ? null : request.getPriorite().trim());
        notification.setLu(false);
        notification.setDateCreation(LocalDateTime.now());
        return mapper.toResponse(repository.save(notification));
    }

    @Transactional
    public NotificationFrancaiseResponse marquerCommeLue(Long id) {
        NotificationFrancaise notification = entite(id);
        notification.setLu(true);
        notification.setDateLecture(LocalDateTime.now());
        return mapper.toResponse(repository.save(notification));
    }

    @Transactional
    public void supprimer(Long id) {
        repository.delete(entite(id));
    }

    @Transactional(readOnly = true)
    public NotificationFrancaiseResponse trouverParId(Long id) {
        return mapper.toResponse(entite(id));
    }

    @Transactional(readOnly = true)
    public List<NotificationFrancaiseResponse> parEmploye(Long employeId) {
        return repository.findByEmployeIdOrderByDateCreationDesc(employeId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationFrancaiseResponse> nonLues(Long employeId) {
        return repository.findByEmployeIdAndLuFalseOrderByDateCreationDesc(employeId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationFrancaiseResponse> visiblesPour(CustomUserDetails utilisateur, boolean uniquementNonLues) {
        boolean visionGlobale = utilisateur.getRole() == RoleType.MANAGER
                || utilisateur.getRole() == RoleType.HR
                || utilisateur.getRole() == RoleType.ADMIN;
        List<NotificationFrancaise> notifications = visionGlobale
                ? (uniquementNonLues ? repository.findByLuFalseOrderByDateCreationDesc() : repository.findAllByOrderByDateCreationDesc())
                : (uniquementNonLues
                        ? repository.findByEmployeIdAndTypeLibelleAndLuFalseOrderByDateCreationDesc(utilisateur.getId(), "DECISION_CONGE")
                        : repository.findByEmployeIdAndTypeLibelleOrderByDateCreationDesc(utilisateur.getId(), "DECISION_CONGE"));
        return notifications.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public NotificationFrancaiseResponse marquerCommeLue(Long id, CustomUserDetails utilisateur) {
        NotificationFrancaise notification = autorisee(id, utilisateur);
        notification.setLu(true);
        notification.setDateLecture(LocalDateTime.now());
        NotificationFrancaiseResponse response = mapper.toResponse(repository.save(notification));
        com.xtensus.hrmanagementapi.notificationfr.realtime.NotificationRealtimePublisher.publierApresCommit();
        return response;
    }

    @Transactional
    public void supprimer(Long id, CustomUserDetails utilisateur) {
        repository.delete(autorisee(id, utilisateur));
        com.xtensus.hrmanagementapi.notificationfr.realtime.NotificationRealtimePublisher.publierApresCommit();
    }

    @Transactional
    public void notifier(Employe destinataire, String typeLibelle, String titre, String contenu, String priorite) {
        if (destinataire == null) return;
        NotificationType type = types.findByLibelle(typeLibelle)
                .orElseThrow(() -> new NotificationTypeIntrouvableException(typeLibelle));
        NotificationFrancaise notification = new NotificationFrancaise();
        notification.setEmploye(destinataire);
        notification.setType(type);
        notification.setTitre(titre);
        notification.setContenu(contenu);
        notification.setPriorite(priorite);
        notification.setLu(false);
        notification.setDateCreation(LocalDateTime.now());
        repository.save(notification);
        com.xtensus.hrmanagementapi.notificationfr.realtime.NotificationRealtimePublisher.publierApresCommit();
    }

    private NotificationFrancaise autorisee(Long id, CustomUserDetails utilisateur) {
        NotificationFrancaise notification = entite(id);
        if (!notification.getEmploye().getId().equals(utilisateur.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Notification non autorisee");
        }
        return notification;
    }

    private NotificationFrancaise entite(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotificationFrancaiseIntrouvableException(id));
    }
}
