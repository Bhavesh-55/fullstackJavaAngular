package com.example.skilldashboard.repository;

import com.example.skilldashboard.model.Skill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class SkillRepositoryTest {

    @Autowired
    private SkillRepository skillRepository;

    @Test
    void existsByNameIgnoreCase_whenSkillExists_shouldReturnTrue() {
        Skill skill = Skill.builder()
                .name("Java")
                .category("Backend")
                .level("Advanced")
                .description("Core backend language")
                .build();

        skillRepository.save(skill);

        boolean exists = skillRepository.existsByNameIgnoreCase("java");

        assertTrue(exists);
    }

    @Test
    void findByNameIgnoreCase_whenSkillExists_shouldReturnSkill() {
        Skill skill = Skill.builder()
                .name("Angular")
                .category("Frontend")
                .level("Intermediate")
                .description("Frontend framework")
                .build();

        skillRepository.save(skill);

        var result = skillRepository.findByNameIgnoreCase("ANGULAR");

        assertTrue(result.isPresent());
        assertEquals("Angular", result.get().getName());
    }
}