package com.xtensus.hrmanagementapi.notification.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationResponse {

    private Long id;

    private NotificationRecipientSummary recipient;

    private String title;

    private String message;

    private Boolean read;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
