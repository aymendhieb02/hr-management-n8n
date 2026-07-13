package com.xtensus.hrmanagementapi.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRecipientSummary {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;
}
