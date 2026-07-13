package com.xtensus.hrmanagementapi.medical.document.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicalDocumentMetadataResponse {

    private Long id;

    private Long leaveRequestId;

    private String originalFilename;

    private String mimeType;

    private Long fileSize;

    private LocalDateTime uploadedAt;
}
