package com.xtensus.hrmanagementapi.medical.document.mapper;

import com.xtensus.hrmanagementapi.domain.entity.MedicalDocument;
import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentMetadataResponse;
import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentResponse;
import org.springframework.stereotype.Component;

@Component
public class MedicalDocumentMapper {

    public MedicalDocumentResponse toResponse(MedicalDocument entity) {
        MedicalDocumentResponse response = new MedicalDocumentResponse();
        response.setId(entity.getId());
        response.setLeaveRequestId(entity.getLeaveRequest().getId());
        response.setOriginalFilename(entity.getOriginalFilename());
        response.setStoredFilename(entity.getStoredFilename());
        response.setMimeType(entity.getMimeType());
        response.setFileSize(entity.getFileSize());
        response.setUploadedAt(entity.getUploadedAt());
        return response;
    }

    public MedicalDocumentMetadataResponse toMetadataResponse(MedicalDocument entity) {
        MedicalDocumentMetadataResponse response = new MedicalDocumentMetadataResponse();
        response.setId(entity.getId());
        response.setLeaveRequestId(entity.getLeaveRequest().getId());
        response.setOriginalFilename(entity.getOriginalFilename());
        response.setMimeType(entity.getMimeType());
        response.setFileSize(entity.getFileSize());
        response.setUploadedAt(entity.getUploadedAt());
        return response;
    }
}
