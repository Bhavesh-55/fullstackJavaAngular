package com.example.skilldashboard.controller;

import com.example.skilldashboard.dto.PageResponse;
import com.example.skilldashboard.dto.SkillRequest;
import com.example.skilldashboard.dto.SkillResponse;
import com.example.skilldashboard.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;


@Tag(name = "Skill APIs", description = "APIs for creating, reading, updating, deleting, searching, sorting, and paginating skills")
@RequestMapping("/api/skills")
@RestController
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @Operation(
            summary = "Create skill",
            description = "Creates a new skill. Only ADMIN can access this API."
    )
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(
            @Valid @RequestBody SkillRequest request
    ) {
        SkillResponse response = skillService.createSkill(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get skills",
            description = "Returns paginated, sorted, and filtered skill list. USER and ADMIN can access this API."
    )
    @GetMapping
    public ResponseEntity<PageResponse<SkillResponse>> getSkills(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<SkillResponse> response = skillService.getSkills(
                category,level,search,page,size,sortBy,sortDir
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get skill by id",
            description = "Returns single skill details by id. USER and ADMIN can access this API."
    )
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        SkillResponse response = skillService.getSkillById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update skill",
            description = "Updates an existing skill. Only ADMIN can access this API."
    )
    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request
    ) {
        SkillResponse response = skillService.updateSkill(id, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Delete skill",
            description = "Deletes a skill by id. Only ADMIN can access this API."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id
    ) {
        skillService.deleteSkill(id);

        return ResponseEntity.noContent().build();
    }
}