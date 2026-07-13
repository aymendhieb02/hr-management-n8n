package com.xtensus.hrmanagementapi.medical.document.service;

import com.xtensus.hrmanagementapi.domain.entity.LeaveRequest;
import com.xtensus.hrmanagementapi.domain.entity.MedicalDocument;
import com.xtensus.hrmanagementapi.leave.request.exception.LeaveRequestNotFoundException;
import com.xtensus.hrmanagementapi.medical.document.config.MedicalDocumentStorageProperties;
import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentMetadataResponse;
import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentResponse;
import com.xtensus.hrmanagementapi.medical.document.exception.DuplicateMedicalDocumentException;
import com.xtensus.hrmanagementapi.medical.document.exception.InvalidMedicalDocumentException;
import com.xtensus.hrmanagementapi.medical.document.exception.MedicalDocumentNotFoundException;
import com.xtensus.hrmanagementapi.medical.document.exception.StorageException;
import com.xtensus.hrmanagementapi.medical.document.mapper.MedicalDocumentMapper;
import com.xtensus.hrmanagementapi.repository.LeaveRequestRepository;
import com.xtensus.hrmanagementapi.repository.MedicalDocumentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MedicalDocumentService {

    private final MedicalDocumentRepository medicalDocumentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final MedicalDocumentMapper medicalDocumentMapper;
    private final MedicalDocumentStorageProperties storageProperties;

    public MedicalDocumentService(
            MedicalDocumentRepository medicalDocumentRepository,
            LeaveRequestRepository leaveRequestRepository,
            MedicalDocumentMapper medicalDocumentMapper,
            MedicalDocumentStorageProperties storageProperties
    ) {
        this.medicalDocumentRepository = medicalDocumentRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.medicalDocumentMapper = medicalDocumentMapper;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public MedicalDocumentResponse upload(Long leaveRequestId, MultipartFile file) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
        medicalDocumentRepository.findByLeaveRequestId(leaveRequestId)
                .ifPresent(existing -> {
                    throw new DuplicateMedicalDocumentException(leaveRequestId);
                });
        validateFile(file);

        Path storageDirectory = storageDirectory();
        createStorageDirectory(storageDirectory);
        String storedFilename = generateStoredFilename(file.getOriginalFilename());
        Path storedPath = storageDirectory.resolve(storedFilename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new StorageException("Failed to store medical document", exception);
        }

        try {
            MedicalDocument document = new MedicalDocument();
            document.setLeaveRequest(leaveRequest);
            document.setOriginalFilename(cleanOriginalFilename(file.getOriginalFilename()));
            document.setStoredFilename(storedFilename);
            document.setStoragePath(storedPath.toAbsolutePath().toString());
            document.setMimeType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadedAt(LocalDateTime.now());

            return medicalDocumentMapper.toResponse(medicalDocumentRepository.save(document));
        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(storedPath);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public MedicalDocumentDownload download(Long id) {
        MedicalDocument document = getDocument(id);
        Path path = Path.of(document.getStoragePath());
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new StorageException("Medical document file is missing");
        }

        Resource resource = new FileSystemResource(path);
        if (!resource.exists() || !resource.isReadable()) {
            throw new StorageException("Medical document file is not readable");
        }

        return new MedicalDocumentDownload(
                resource,
                document.getOriginalFilename(),
                document.getMimeType(),
                document.getFileSize()
        );
    }

    @Transactional
    public void delete(Long id) {
        MedicalDocument document = getDocument(id);
        Path path = Path.of(document.getStoragePath());

        try {
            Files.delete(path);
        } catch (IOException exception) {
            throw new StorageException("Failed to delete medical document file", exception);
        }

        medicalDocumentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public MedicalDocumentMetadataResponse findByLeaveRequest(Long leaveRequestId) {
        MedicalDocument document = medicalDocumentRepository.findByLeaveRequestId(leaveRequestId)
                .orElseThrow(() -> new MedicalDocumentNotFoundException(leaveRequestId));
        return medicalDocumentMapper.toMetadataResponse(document);
    }

    @Transactional(readOnly = true)
    public MedicalDocumentMetadataResponse findById(Long id) {
        return medicalDocumentMapper.toMetadataResponse(getDocument(id));
    }

    private MedicalDocument getDocument(Long id) {
        return medicalDocumentRepository.findById(id)
                .orElseThrow(() -> new MedicalDocumentNotFoundException(id));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidMedicalDocumentException("Medical document file is required");
        }

        if (!storageProperties.getAllowedTypes().contains(file.getContentType())) {
            throw new InvalidMedicalDocumentException("Unsupported medical document type");
        }

        if (file.getSize() > storageProperties.getMaxFileSize().toBytes()) {
            throw new InvalidMedicalDocumentException("Medical document exceeds maximum allowed size");
        }
    }

    private Path storageDirectory() {
        return Path.of(storageProperties.getMedicalCertificatesPath()).toAbsolutePath().normalize();
    }

    private void createStorageDirectory(Path storageDirectory) {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new StorageException("Failed to create medical document storage directory", exception);
        }
    }

    private String generateStoredFilename(String originalFilename) {
        String extension = extensionOf(originalFilename);
        return UUID.randomUUID() + extension;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String cleanOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "medical-document";
        }

        return Path.of(originalFilename).getFileName().toString();
    }

    private void deleteStoredFileQuietly(Path storedPath) {
        try {
            Files.deleteIfExists(storedPath);
        } catch (IOException ignored) {
            // Best effort cleanup after a failed metadata save.
        }
    }
}
