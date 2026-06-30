package com.example.skilldashboard.repository;

import com.example.skilldashboard.model.SkillDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillDocumentRepository extends JpaRepository<SkillDocument, Long> {

    List<SkillDocument> findBySkillIdOrderByUploadedAtDesc(Long skillId);
}