package com.example.skilldashboard.service.impl;

import com.example.skilldashboard.dto.SkillDocumentResponse;
import com.example.skilldashboard.model.Skill;
import com.example.skilldashboard.model.SkillDocument;
import com.example.skilldashboard.repository.SkillDocumentRepository;
import com.example.skilldashboard.repository.SkillRepository;
import com.example.skilldashboard.service.SkillDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class SkillDocumentServiceImpl implements SkillDocumentService {

    private final SkillRepository skillRepository;
    private final SkillDocumentRepository skillDocumentRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @Override
    @Transactional
    public SkillDocumentResponse uploadDocument(Long skillId, MultipartFile file) {

        log.info("File upload request received for skillId: {}", skillId);
        validateFile(file);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + skillId));

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            Files.createDirectories(uploadPath);

            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

            String storedFileName = UUID.randomUUID() + "-" + originalFileName;

            Path targetPath = uploadPath.resolve(storedFileName).normalize();
            log.info("Target file path: {}", targetPath);
            if (!targetPath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid file path");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File copied successfully to path: {}", targetPath);
            SkillDocument document = SkillDocument.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .filePath(targetPath.toString())
                    .skill(skill)
                    .build();

            SkillDocument savedDocument = skillDocumentRepository.save(document);
            log.info("File metadata saved successfully. documentId: {}", savedDocument.getId());
            return toResponse(savedDocument);

        } catch (Exception exception) {
            throw new RuntimeException("Unable to upload file: " + exception.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDocumentResponse> getDocumentsBySkillId(Long skillId) {
        if (!skillRepository.existsById(skillId)) {
            throw new RuntimeException("Skill not found with id: " + skillId);
        }

        return skillDocumentRepository.findBySkillIdOrderByUploadedAtDesc(skillId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) {
        SkillDocument document = skillDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        try {
            Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File is not available for download");
            }

            return resource;

        } catch (Exception exception) {
            throw new RuntimeException("Unable to download file: " + exception.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SkillDocumentResponse getDocumentById(Long documentId) {
        SkillDocument document = skillDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        return toResponse(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        SkillDocument document = skillDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        try {
            Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();

            Files.deleteIfExists(filePath);

            skillDocumentRepository.delete(document);

        } catch (Exception exception) {
            throw new RuntimeException("Unable to delete file: " + exception.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File validation failed: file is empty");
            throw new RuntimeException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File validation failed: file size {} exceeds limit {}", file.getSize(), MAX_FILE_SIZE);
            throw new RuntimeException("File size cannot exceed 5MB");
        }

        log.info("Validating file content type: {}", file.getContentType());

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            log.warn("File validation failed: content type {} is not allowed", file.getContentType());
            throw new RuntimeException("Only PDF, PNG, and JPG files are allowed");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            log.warn("File validation failed: invalid file name");
            throw new RuntimeException("Invalid file name");
        }

        String cleanFileName = StringUtils.cleanPath(originalFileName);

        if (cleanFileName.contains("..")) {
            log.warn("File validation failed: file name contains invalid path sequence. fileName: {}", cleanFileName);
            throw new RuntimeException("Invalid file path sequence");
        }

        log.info("File validation successful. fileName: {}", cleanFileName);
    }

    private SkillDocumentResponse toResponse(SkillDocument document) {
        return SkillDocumentResponse.builder()
                .id(document.getId())
                .skillId(document.getSkill().getId())
                .originalFileName(document.getOriginalFileName())
                .contentType(document.getContentType())
                .size(document.getSize())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}