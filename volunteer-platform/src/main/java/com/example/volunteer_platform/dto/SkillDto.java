package com.example.volunteer_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for Skill.
 *
 * This class is used to safely transfer skill-related data
 * between the client layer and the backend without exposing
 * the internal Skill entity directly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDto {

    /**
     * Name of the skill.
     * 
     * - Must not be blank
     * - Maximum length is restricted to 100 characters
     */
    @NotBlank
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String name;
}
