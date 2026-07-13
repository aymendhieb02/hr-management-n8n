package com.xtensus.hrmanagementapi.notification.mapper;

import com.xtensus.hrmanagementapi.domain.entity.Notification;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.notification.dto.NotificationCreateRequest;
import com.xtensus.hrmanagementapi.notification.dto.NotificationRecipientSummary;
import com.xtensus.hrmanagementapi.notification.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.setTitle(request.getTitle().trim());
        notification.setMessage(request.getMessage().trim());
        return notification;
    }

    public NotificationResponse toResponse(Notification entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setRecipient(toRecipientSummary(entity.getRecipient()));
        response.setTitle(entity.getTitle());
        response.setMessage(entity.getMessage());
        response.setRead(entity.getRead());
        response.setCreatedAt(entity.getCreatedAt());
        response.setReadAt(entity.getReadAt());
        return response;
    }

    private NotificationRecipientSummary toRecipientSummary(User user) {
        NotificationRecipientSummary summary = new NotificationRecipientSummary();
        summary.setId(user.getId());
        summary.setFirstName(user.getFirstName());
        summary.setLastName(user.getLastName());
        summary.setEmail(user.getEmail());
        return summary;
    }
}
