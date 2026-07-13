package com.xtensus.hrmanagementapi.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationCreateRequest {

    @NotNull
    private Long recipientId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String message;
}
