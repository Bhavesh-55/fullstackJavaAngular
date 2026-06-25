package com.example.skilldashboard.service.impl;

import com.example.skilldashboard.dto.SkillRequest;
import com.example.skilldashboard.dto.SkillResponse;
import com.example.skilldashboard.exception.DuplicateResourceException;
import com.example.skilldashboard.exception.ResourceNotFoundException;
import com.example.skilldashboard.mapper.SkillMapper;
import com.example.skilldashboard.model.Skill;
import com.example.skilldashboard.repository.SkillRepository;
import com.example.skilldashboard.service.SkillService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.example.skilldashboard.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public SkillServiceImpl(SkillRepository skillRepository, SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.skillMapper = skillMapper;
    }

    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        String skillName = request.getName().trim();

        if (skillRepository.existsByNameIgnoreCase(skillName)) {
            throw new DuplicateResourceException("Skill already exists with name: " + skillName);
        }

        Skill skill = skillMapper.toEntity(request);
        Skill savedSkill = skillRepository.save(skill);

        return skillMapper.toResponse(savedSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SkillResponse> getSkills(
            String category,
            String level,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        String normalizedCategory = normalize(category);
        String normalizedLevel = normalize(level);
        String normalizedSearch = normalize(search);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Skill> skillPage = skillRepository.searchSkills(
                normalizedCategory,
                normalizedLevel,
                normalizedSearch,
                pageable
        );

        List<SkillResponse> content = skillPage
                .getContent()
                .stream()
                .map(skillMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                skillPage.getNumber(),
                skillPage.getSize(),
                skillPage.getTotalElements(),
                skillPage.getTotalPages(),
                skillPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        return skillMapper.toResponse(skill);
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        String newSkillName = request.getName().trim();

        skillRepository.findByNameIgnoreCase(newSkillName)
                .filter(skill -> !skill.getId().equals(id))
                .ifPresent(skill -> {
                    throw new DuplicateResourceException("Skill already exists with name: " + newSkillName);
                });

        skillMapper.updateEntity(existingSkill, request);

        Skill updatedSkill = skillRepository.save(existingSkill);

        return skillMapper.toResponse(updatedSkill);
    }

    @Override
    @Transactional
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found with id: " + id);
        }

        skillRepository.deleteById(id);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}