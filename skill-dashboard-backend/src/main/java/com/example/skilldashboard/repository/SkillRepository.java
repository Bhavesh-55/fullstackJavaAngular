package com.example.skilldashboard.repository;

import com.example.skilldashboard.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Skill> findByNameIgnoreCase(String name);

    @Query("""
            SELECT s
            FROM Skill s
            WHERE (:category IS NULL OR LOWER(s.category) = LOWER(:category))
              AND (:level IS NULL OR LOWER(s.level) = LOWER(:level))
              AND (
                    :search IS NULL
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.category) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.level) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            ORDER BY s.id ASC
            """)
    Page<Skill> searchSkills(
            @Param("category") String category,
            @Param("level") String level,
            @Param("search") String search, Pageable pageable
    );
}