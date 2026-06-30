package com.example.skilldashboard.service;

import com.example.skilldashboard.dto.SkillDocumentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SkillDocumentService {

    SkillDocumentResponse uploadDocument(Long skillId, MultipartFile file);

    List<SkillDocumentResponse> getDocumentsBySkillId(Long skillId);

    Resource downloadDocument(Long documentId);

    SkillDocumentResponse getDocumentById(Long documentId);

    void deleteDocument(Long documentId);
}