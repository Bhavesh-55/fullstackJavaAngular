package com.example.skilldashboard.service.impl;

import com.example.skilldashboard.dto.SkillDocumentResponse;
import com.example.skilldashboard.model.Skill;
import com.example.skilldashboard.model.SkillDocument;
import com.example.skilldashboard.repository.SkillDocumentRepository;
import com.example.skilldashboard.repository.SkillRepository;
import com.example.skilldashboard.service.SkillDocumentService;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillDocumentServiceImpl implements SkillDocumentService {

    private final SkillRepository skillRepository;
    private final SkillDocumentRepository skillDocumentRepository;
    private final Storage storage;

    @Value("${app.gcp.storage.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @Override
    @Transactional
    public SkillDocumentResponse uploadDocument(Long skillId, MultipartFile file) {
        validateFile(file);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + skillId));

        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

            String storedFileName = UUID.randomUUID() + "-" + originalFileName;

            String objectName = "skills/"
                    + skillId
                    + "/documents/"
                    + storedFileName;

            BlobId blobId = BlobId.of(bucketName, objectName);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setMetadata(java.util.Map.of(
                            "originalFileName", originalFileName,
                            "skillId", String.valueOf(skillId)
                    ))
                    .build();

            storage.create(blobInfo, file.getBytes());

            SkillDocument document = SkillDocument.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .skill(skill)
                    .build();

            SkillDocument savedDocument = skillDocumentRepository.save(document);

            return toResponse(savedDocument);

        } catch (Exception exception) {
            throw new RuntimeException("Unable to upload file to Google Cloud Storage: " + exception.getMessage());
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

        if (document.getBucketName() == null || document.getObjectName() == null) {
            throw new RuntimeException("This document was uploaded using old local storage. Please upload it again.");
        }

        Blob blob = storage.get(
                BlobId.of(document.getBucketName(), document.getObjectName())
        );

        if (blob == null || !blob.exists()) {
            throw new RuntimeException("File not found in Google Cloud Storage");
        }

        byte[] content = blob.getContent();

        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return document.getOriginalFileName();
            }
        };
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

        if (document.getBucketName() != null && document.getObjectName() != null) {
            storage.delete(
                    BlobId.of(document.getBucketName(), document.getObjectName())
            );
        }

        skillDocumentRepository.delete(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size cannot exceed 5MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Only PDF, PNG, and JPG files are allowed");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }

        String cleanFileName = StringUtils.cleanPath(originalFileName);

        if (cleanFileName.contains("..")) {
            throw new RuntimeException("Invalid file path sequence");
        }
    }

    private SkillDocumentResponse toResponse(SkillDocument document) {
        return SkillDocumentResponse.builder()
                .id(document.getId())
                .skillId(document.getSkill().getId())
                .originalFileName(document.getOriginalFileName())
                .contentType(document.getContentType())
                .size(document.getSize())
                .uploadedAt(document.getUploadedAt())
                .storageProvider(document.getBucketName() != null ? "GCS" : "LOCAL")
                .build();
    }
}