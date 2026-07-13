package com.xtensus.hrmanagementapi.medical.document.service;

import org.springframework.core.io.Resource;

public record MedicalDocumentDownload(
        Resource resource,
        String originalFilename,
        String mimeType,
        Long fileSize
) {
}
