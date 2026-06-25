package com.example.skilldashboard.controller;

import com.example.skilldashboard.model.Skill;
import com.example.skilldashboard.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SkillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SkillRepository skillRepository;

    @BeforeEach
    void setUp() {
        skillRepository.deleteAll();

        Skill javaSkill = Skill.builder()
                .name("Java")
                .category("Backend")
                .level("Advanced")
                .description("Core backend language")
                .build();

        Skill angularSkill = Skill.builder()
                .name("Angular")
                .category("Frontend")
                .level("Intermediate")
                .description("Frontend framework")
                .build();

        skillRepository.save(javaSkill);
        skillRepository.save(angularSkill);
    }

    @Test
    void getSkills_withoutLogin_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSkills_withUserRole_shouldReturnSkills() throws Exception {
        mockMvc.perform(get("/api/skills")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createSkill_withUserRole_shouldReturnForbidden() throws Exception {
        String requestBody = """
                {
                  "name": "Spring Security",
                  "category": "Backend",
                  "level": "Advanced",
                  "description": "Security framework"
                }
                """;

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSkill_withAdminRole_shouldCreateSkill() throws Exception {
        String requestBody = """
                {
                  "name": "Spring Security",
                  "category": "Backend",
                  "level": "Advanced",
                  "description": "Security framework"
                }
                """;

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring Security"))
                .andExpect(jsonPath("$.category").value("Backend"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSkill_withAdminRole_shouldDeleteSkill() throws Exception {
        Skill skill = skillRepository.findAll().get(0);

        mockMvc.perform(delete("/api/skills/{id}", skill.getId()))
                .andExpect(status().isNoContent());
    }
}