package com.xtensus.hrmanagementapi.medical.document.controller;

import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentMetadataResponse;
import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentResponse;
import com.xtensus.hrmanagementapi.medical.document.service.MedicalDocumentDownload;
import com.xtensus.hrmanagementapi.medical.document.service.MedicalDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/medical-documents", "/api/certificats-medicaux"})
public class MedicalDocumentController {

    private final MedicalDocumentService medicalDocumentService;

    public MedicalDocumentController(MedicalDocumentService medicalDocumentService) {
        this.medicalDocumentService = medicalDocumentService;
    }

    @PostMapping(value = "/upload/{leaveRequestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalDocumentResponse> upload(
            @PathVariable Long leaveRequestId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(medicalDocumentService.upload(leaveRequestId, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalDocumentMetadataResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalDocumentService.findById(id));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        MedicalDocumentDownload download = medicalDocumentService.download(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .contentLength(download.fileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.originalFilename())
                                .build()
                                .toString()
                )
                .body(download.resource());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicalDocumentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

