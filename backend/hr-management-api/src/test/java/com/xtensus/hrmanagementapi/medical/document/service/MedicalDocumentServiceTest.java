package com.xtensus.hrmanagementapi.medical.document.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.domain.entity.LeaveRequest;
import com.xtensus.hrmanagementapi.domain.entity.MedicalDocument;
import com.xtensus.hrmanagementapi.leave.request.exception.LeaveRequestNotFoundException;
import com.xtensus.hrmanagementapi.medical.document.config.MedicalDocumentStorageProperties;
import com.xtensus.hrmanagementapi.medical.document.dto.MedicalDocumentResponse;
import com.xtensus.hrmanagementapi.medical.document.exception.DuplicateMedicalDocumentException;
import com.xtensus.hrmanagementapi.medical.document.exception.InvalidMedicalDocumentException;
import com.xtensus.hrmanagementapi.medical.document.exception.MedicalDocumentNotFoundException;
import com.xtensus.hrmanagementapi.medical.document.exception.StorageException;
import com.xtensus.hrmanagementapi.medical.document.mapper.MedicalDocumentMapper;
import com.xtensus.hrmanagementapi.repository.LeaveRequestRepository;
import com.xtensus.hrmanagementapi.repository.MedicalDocumentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

@ExtendWith(MockitoExtension.class)
class MedicalDocumentServiceTest {

    @TempDir
    private Path tempDir;

    @Mock
    private MedicalDocumentRepository medicalDocumentRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    private MedicalDocumentStorageProperties storageProperties;

    private MedicalDocumentService medicalDocumentService;

    @BeforeEach
    void setUp() {
        storageProperties = new MedicalDocumentStorageProperties();
        storageProperties.setMedicalCertificatesPath(tempDir.resolve("medical").toString());
        storageProperties.setMaxFileSize(DataSize.ofMegabytes(5));
        storageProperties.setAllowedTypes(List.of("application/pdf", "image/jpeg", "image/png"));

        medicalDocumentService = new MedicalDocumentService(
                medicalDocumentRepository,
                leaveRequestRepository,
                new MedicalDocumentMapper(),
                storageProperties
        );
    }

    @Test
    void successfulUploadStoresFileAndMetadata() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.empty());
        when(medicalDocumentRepository.save(any(MedicalDocument.class))).thenAnswer(invocation -> {
            MedicalDocument document = invocation.getArgument(0);
            document.setId(10L);
            return document;
        });

        MedicalDocumentResponse response = medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "pdf"));

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getLeaveRequestId());
        assertEquals("certificate.pdf", response.getOriginalFilename());
        assertTrue(Files.exists(Path.of(storageProperties.getMedicalCertificatesPath()).resolve(response.getStoredFilename())));
    }

    @Test
    void successfulDownloadReturnsResource() throws IOException {
        Path file = createStoredFile("stored.pdf", "pdf");
        when(medicalDocumentRepository.findById(10L)).thenReturn(Optional.of(document(10L, file)));

        MedicalDocumentDownload download = medicalDocumentService.download(10L);

        assertTrue(download.resource().exists());
        assertEquals("certificate.pdf", download.originalFilename());
        assertEquals("application/pdf", download.mimeType());
    }

    @Test
    void successfulDeleteRemovesFileAndMetadata() throws IOException {
        Path file = createStoredFile("stored.pdf", "pdf");
        MedicalDocument document = document(10L, file);
        when(medicalDocumentRepository.findById(10L)).thenReturn(Optional.of(document));

        medicalDocumentService.delete(10L);

        assertFalse(Files.exists(file));
        verify(medicalDocumentRepository).delete(document);
    }

    @Test
    void duplicateUploadReturnsConflict() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.of(document(10L, tempDir.resolve("x.pdf"))));

        assertThrows(DuplicateMedicalDocumentException.class,
                () -> medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "pdf")));
    }

    @Test
    void missingLeaveRequestReturnsNotFound() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LeaveRequestNotFoundException.class,
                () -> medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "pdf")));
    }

    @Test
    void missingDocumentReturnsNotFound() {
        when(medicalDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MedicalDocumentNotFoundException.class, () -> medicalDocumentService.findById(99L));
    }

    @Test
    void invalidMimeTypeReturnsBadRequest() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidMedicalDocumentException.class,
                () -> medicalDocumentService.upload(1L, file("notes.txt", "text/plain", "hello")));
    }

    @Test
    void emptyUploadReturnsBadRequest() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidMedicalDocumentException.class,
                () -> medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "")));
    }

    @Test
    void oversizedUploadReturnsBadRequest() {
        storageProperties.setMaxFileSize(DataSize.ofBytes(2));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidMedicalDocumentException.class,
                () -> medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "pdf")));
    }

    @Test
    void filesystemFailureReturnsStorageException() throws IOException {
        Path pathAsFile = tempDir.resolve("not-a-directory");
        Files.writeString(pathAsFile, "blocked");
        storageProperties.setMedicalCertificatesPath(pathAsFile.toString());
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.empty());

        assertThrows(StorageException.class,
                () -> medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "pdf")));
        verify(medicalDocumentRepository, never()).save(any(MedicalDocument.class));
    }

    @Test
    void metadataRollbackRemovesStoredFile() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest(1L)));
        when(medicalDocumentRepository.findByLeaveRequestId(1L)).thenReturn(Optional.empty());
        when(medicalDocumentRepository.save(any(MedicalDocument.class))).thenThrow(new RuntimeException("db failed"));

        assertThrows(RuntimeException.class,
                () -> medicalDocumentService.upload(1L, file("certificate.pdf", "application/pdf", "pdf")));
        assertEquals(0, countFiles(Path.of(storageProperties.getMedicalCertificatesPath())));
    }

    @Test
    void deleteRollbackWhenFilesystemDeleteFails() throws IOException {
        Path directory = tempDir.resolve("not-empty");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("child.txt"), "child");
        MedicalDocument document = document(10L, directory);
        when(medicalDocumentRepository.findById(10L)).thenReturn(Optional.of(document));

        assertThrows(StorageException.class, () -> medicalDocumentService.delete(10L));
        verify(medicalDocumentRepository, never()).delete(document);
    }

    @Test
    void downloadMissingFilesystemFileReturnsStorageException() {
        when(medicalDocumentRepository.findById(10L)).thenReturn(Optional.of(document(10L, tempDir.resolve("missing.pdf"))));

        assertThrows(StorageException.class, () -> medicalDocumentService.download(10L));
    }

    private MockMultipartFile file(String filename, String contentType, String content) {
        return new MockMultipartFile("file", filename, contentType, content.getBytes());
    }

    private Path createStoredFile(String filename, String content) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, content);
        return file;
    }

    private long countFiles(Path directory) {
        if (!Files.exists(directory)) {
            return 0;
        }

        try (var stream = Files.list(directory)) {
            return stream.count();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private MedicalDocument document(Long id, Path path) {
        MedicalDocument document = new MedicalDocument();
        document.setId(id);
        document.setLeaveRequest(leaveRequest(1L));
        document.setOriginalFilename("certificate.pdf");
        document.setStoredFilename(path.getFileName().toString());
        document.setStoragePath(path.toString());
        document.setMimeType("application/pdf");
        document.setFileSize(3L);
        document.setUploadedAt(LocalDateTime.now());
        return document;
    }

    private LeaveRequest leaveRequest(Long id) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(id);
        return leaveRequest;
    }
}
