package com.example.skilldashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(min = 2, max = 100, message = "Skill name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Skill name must contain only alphabets and spaces"
    )
    private String name;

    @NotBlank(message = "Category is required")
    @Pattern(
            regexp = "Frontend|Backend|Cloud|Database|DevOps",
            message = "Category must be Frontend, Backend, Cloud, Database, or DevOps"
    )
    private String category;

    @NotBlank(message = "Level is required")
    @Pattern(
            regexp = "Beginner|Intermediate|Advanced",
            message = "Level must be Beginner, Intermediate, or Advanced"
    )
    private String level;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public SkillRequest() {
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}