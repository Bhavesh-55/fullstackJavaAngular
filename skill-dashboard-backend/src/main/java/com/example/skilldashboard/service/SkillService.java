package com.example.skilldashboard.service;

import com.example.skilldashboard.dto.PageResponse;
import com.example.skilldashboard.dto.SkillRequest;
import com.example.skilldashboard.dto.SkillResponse;

import java.util.List;

public interface SkillService {

    SkillResponse createSkill(SkillRequest request);

    PageResponse<SkillResponse> getSkills(String category, String level, String search, int page, int size, String sortBy, String sortDir);

    SkillResponse getSkillById(Long id);

    SkillResponse updateSkill(Long id, SkillRequest request);

    void deleteSkill(Long id);
}