package com.example.skilldashboard.service;

import com.example.skilldashboard.dto.SkillRequest;
import com.example.skilldashboard.dto.SkillResponse;
import com.example.skilldashboard.mapper.SkillMapper;
import com.example.skilldashboard.model.Skill;
import com.example.skilldashboard.repository.SkillRepository;
import com.example.skilldashboard.service.impl.SkillServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void createSkill_whenSkillNameIsUnique_shouldSaveSkill() {
        SkillRequest request = new SkillRequest();
        request.setName("Spring Boot");
        request.setCategory("Backend");
        request.setLevel("Advanced");
        request.setDescription("Java backend framework");

        Skill skillToSave = Skill.builder()
                .name("Spring Boot")
                .category("Backend")
                .level("Advanced")
                .description("Java backend framework")
                .build();

        Skill savedSkill = Skill.builder()
                .id(1L)
                .name("Spring Boot")
                .category("Backend")
                .level("Advanced")
                .description("Java backend framework")
                .build();

        SkillResponse expectedResponse = SkillResponse.builder()
                .id(1L)
                .name("Spring Boot")
                .category("Backend")
                .level("Advanced")
                .description("Java backend framework")
                .build();

        when(skillRepository.existsByNameIgnoreCase("Spring Boot"))
                .thenReturn(false);

        when(skillMapper.toEntity(request))
                .thenReturn(skillToSave);

        when(skillRepository.save(skillToSave))
                .thenReturn(savedSkill);

        when(skillMapper.toResponse(savedSkill))
                .thenReturn(expectedResponse);

        SkillResponse actualResponse = skillService.createSkill(request);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertEquals("Spring Boot", actualResponse.getName());
        assertEquals("Backend", actualResponse.getCategory());
        assertEquals("Advanced", actualResponse.getLevel());

        verify(skillRepository).existsByNameIgnoreCase("Spring Boot");
        verify(skillMapper).toEntity(request);
        verify(skillRepository).save(skillToSave);
        verify(skillMapper).toResponse(savedSkill);
    }
}