package com.xtensus.hrmanagementapi.medical.document.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicalDocumentResponse {

    private Long id;

    private Long leaveRequestId;

    private String originalFilename;

    private String storedFilename;

    private String mimeType;

    private Long fileSize;

    private LocalDateTime uploadedAt;
}
