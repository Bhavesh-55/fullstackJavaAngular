package com.example.skilldashboard.controller;

import com.example.skilldashboard.dto.SkillDocumentResponse;
import com.example.skilldashboard.service.SkillDocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Skill Document APIs", description = "APIs for uploading, listing, downloading, and deleting skill documents")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillDocumentController {

    private final SkillDocumentService skillDocumentService;


    @Operation(
            summary = "Upload skill document",
            description = "Uploads PDF, PNG, or JPG document for a skill. Only ADMIN can access this API."
    )
    @PostMapping("/{skillId}/documents")
    public ResponseEntity<SkillDocumentResponse> uploadDocument(
            @PathVariable Long skillId,
            @RequestParam("file") MultipartFile file
    ) {
        SkillDocumentResponse response = skillDocumentService.uploadDocument(skillId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Get documents by skill id",
            description = "Returns uploaded documents for a skill. USER and ADMIN can access this API."
    )
    @GetMapping("/{skillId}/documents")
    public ResponseEntity<List<SkillDocumentResponse>> getDocuments(
            @PathVariable Long skillId
    ) {
        List<SkillDocumentResponse> documents =
                skillDocumentService.getDocumentsBySkillId(skillId);

        return ResponseEntity.ok(documents);
    }

    @Operation(
            summary = "Download skill document",
            description = "Downloads a document by document id. USER and ADMIN can access this API."
    )
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId
    ) {
        SkillDocumentResponse document = skillDocumentService.getDocumentById(documentId);

        Resource resource = skillDocumentService.downloadDocument(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\""
                )
                .body(resource);
    }


    @Operation(
            summary = "Delete skill document",
            description = "Deletes uploaded document. Only ADMIN can access this API."
    )
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId
    ) {
        skillDocumentService.deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }
}