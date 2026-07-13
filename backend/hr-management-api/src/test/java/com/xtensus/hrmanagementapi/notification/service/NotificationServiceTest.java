package com.xtensus.hrmanagementapi.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.domain.entity.Notification;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.notification.dto.NotificationCreateRequest;
import com.xtensus.hrmanagementapi.notification.dto.NotificationResponse;
import com.xtensus.hrmanagementapi.notification.exception.NotificationNotFoundException;
import com.xtensus.hrmanagementapi.notification.mapper.NotificationMapper;
import com.xtensus.hrmanagementapi.repository.NotificationRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                userRepository,
                new NotificationMapper()
        );
    }

    @Test
    void notificationCreationSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(10L);
            return notification;
        });

        NotificationResponse response = notificationService.create(request());

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getRecipient().getId());
        assertEquals("New Leave Request", response.getTitle());
    }

    @Test
    void notificationUnreadByDefault() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.create(request());

        assertEquals(false, response.getRead());
        assertNull(response.getReadAt());
    }

    @Test
    void missingRecipientReturnsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> notificationService.create(request()));
    }

    @Test
    void findRecipientNotifications() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification(10L, false)));

        List<NotificationResponse> responses = notificationService.findByRecipient(1L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
    }

    @Test
    void findUnreadNotifications() {
        when(notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification(10L, false)));

        List<NotificationResponse> responses = notificationService.findUnread(1L);

        assertEquals(1, responses.size());
        assertEquals(false, responses.get(0).getRead());
    }

    @Test
    void markAsRead() {
        Notification notification = notification(10L, false);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(10L);

        assertEquals(true, response.getRead());
        assertNotNull(response.getReadAt());
    }

    @Test
    void markAllAsRead() {
        when(notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification(10L, false), notification(11L, false)));
        when(notificationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationResponse> responses = notificationService.markAllAsRead(1L);

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(NotificationResponse::getRead));
        assertTrue(responses.stream().allMatch(response -> response.getReadAt() != null));
    }

    @Test
    void callingMarkAsReadTwiceDoesNotChangeReadAt() {
        Notification notification = notification(10L, true);
        LocalDateTime originalReadAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        notification.setReadAt(originalReadAt);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(10L);

        assertEquals(originalReadAt, response.getReadAt());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void deleteNotification() {
        Notification notification = notification(10L, false);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.delete(10L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void missingNotificationReturnsNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.findById(99L));
    }

    private NotificationCreateRequest request() {
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(1L);
        request.setTitle("New Leave Request");
        request.setMessage("Jane Doe submitted a leave request.");
        return request;
    }

    private Notification notification(Long id, boolean read) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setRecipient(user(1L));
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadAt(read ? LocalDateTime.now() : null);
        return notification;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setRole(RoleType.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        return user;
    }
}
