package com.xtensus.hrmanagementapi.notification.service;

import com.xtensus.hrmanagementapi.domain.entity.Notification;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.notification.dto.NotificationCreateRequest;
import com.xtensus.hrmanagementapi.notification.dto.NotificationResponse;
import com.xtensus.hrmanagementapi.notification.exception.NotificationNotFoundException;
import com.xtensus.hrmanagementapi.notification.mapper.NotificationMapper;
import com.xtensus.hrmanagementapi.repository.NotificationRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new UserNotFoundException(request.getRecipientId()));

        Notification notification = notificationMapper.toEntity(request);
        notification.setRecipient(recipient);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadAt(null);

        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(Long id) {
        return notificationMapper.toResponse(getNotification(id));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByRecipient(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findUnread(Long userId) {
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = getNotification(id);
        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public List<NotificationResponse> markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime readAt = LocalDateTime.now();
        notifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(readAt);
        });

        return notificationRepository.saveAll(notifications)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Notification notification = getNotification(id);
        notificationRepository.delete(notification);
    }

    private Notification getNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
