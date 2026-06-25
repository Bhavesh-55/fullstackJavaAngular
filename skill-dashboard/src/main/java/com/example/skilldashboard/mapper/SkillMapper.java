package com.example.skilldashboard.mapper;

import com.example.skilldashboard.dto.SkillRequest;
import com.example.skilldashboard.dto.SkillResponse;
import com.example.skilldashboard.model.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    public Skill toEntity(SkillRequest request) {
        Skill skill = new Skill();

        skill.setName(request.getName().trim());
        skill.setCategory(request.getCategory());
        skill.setLevel(request.getLevel());
        skill.setDescription(request.getDescription());

        return skill;
    }

    public void updateEntity(Skill skill, SkillRequest request) {
        skill.setName(request.getName().trim());
        skill.setCategory(request.getCategory());
        skill.setLevel(request.getLevel());
        skill.setDescription(request.getDescription());
    }

    public SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getLevel(),
                skill.getDescription(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}