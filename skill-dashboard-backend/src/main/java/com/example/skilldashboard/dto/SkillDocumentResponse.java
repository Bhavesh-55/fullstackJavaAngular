package com.example.skilldashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SkillDocumentResponse {

    private Long id;
    //We do not return filePath to frontend
    private Long skillId;

    private String originalFileName;

    private String contentType;

    private Long size;

    private LocalDateTime uploadedAt;
    private String storageProvider;
}