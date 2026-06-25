package com.example.skilldashboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResponse {

    private Long id;
    private String name;
    private String category;
    private String level;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}