package com.example.volunteer_platform.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.volunteer_platform.dto.SkillDto;
import com.example.volunteer_platform.model.Skill;
import com.example.volunteer_platform.model.Task;
import com.example.volunteer_platform.model.Volunteer;
import com.example.volunteer_platform.service.SkillService;
import com.example.volunteer_platform.service.TaskService;
import com.example.volunteer_platform.service.UserService;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing skills in the community volunteering platform.
 * Handles CRUD operations for skills and manages skill associations
 * with volunteers and tasks.
 */
@RestController
@RequestMapping
public class SkillController {

    // Service for skill-related operations
    @Autowired
    private SkillService skillService;

    // Service for volunteer-related operations
    @Autowired
    private UserService userService;

    // Service for task-related operations
    @Autowired
    private TaskService taskService;

    /**
     * Retrieve all available skills.
     */
    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        List<Skill> skills = skillService.getAllSkills();

        // Return 204 if no skills are present
        if (skills.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(skills, HttpStatus.OK);
    }

    /**
     * Fetch a skill by name.
     * If the skill does not exist, it is created automatically.
     */
    @GetMapping("/skills/name/{name}")
    public ResponseEntity<Skill> getSkillByName(@PathVariable String name) {

        // Attempt to find the skill by name
        Skill skill = skillService.findByName(name).orElse(null);

        if (skill != null) {
            return new ResponseEntity<>(skill, HttpStatus.OK);
        }

        // Auto-create skill if it does not exist
        SkillDto newSkillDto = new SkillDto(name);
        return saveSkill(newSkillDto);
    }

    /**
     * Retrieve a skill using its unique ID.
     */
    @GetMapping("/skills/id/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        Skill skill = skillService.findById(id).orElse(null);

        return (skill != null)
                ? new ResponseEntity<>(skill, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Create and persist a new skill.
     */
    @PostMapping("/skills")
    @Transactional
    public ResponseEntity<Skill> saveSkill(@RequestBody @Valid SkillDto skillDto) {

        // Prevent duplicate skill creation
        if (skillService.findByName(skillDto.getName()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        try {
            // Normalize skill name before saving
            Skill skill = Skill.builder()
                    .name(skillDto.getName().toLowerCase())
                    .build();

            skillService.saveSkill(skill);
            return new ResponseEntity<>(skill, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete a skill by ID.
     */
    @DeleteMapping("/skills/id/{id}")
    @Transactional
    public ResponseEntity<Void> deleteSkillById(@PathVariable Long id) {

        Skill skill = skillService.findById(id).orElse(null);

        if (skill != null) {
            skillService.deleteSkillById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ---------------- Volunteer Skill APIs ----------------

    /**
     * Retrieve all skills associated with a volunteer.
     */
    @GetMapping("/volunteers/{volunteerId}/skills")
    public ResponseEntity<List<Skill>> getVolunteerSkills(@PathVariable Long volunteerId) {

        Optional<Volunteer> volunteerOpt = userService.findVolunteerById(volunteerId);

        if (volunteerOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Convert skill set to list for response
        List<Skill> skills = new ArrayList<>(volunteerOpt.get().getSkills());
        return new ResponseEntity<>(skills, HttpStatus.OK);
    }

    /**
     * Add a skill to a volunteer.
     * Skill is created if it does not already exist.
     */
    @PostMapping("/volunteers/{volunteerId}/skills")
    @Transactional
    public ResponseEntity<Volunteer> addSkillToVolunteer(
            @PathVariable Long volunteerId,
            @RequestBody @Valid SkillDto skillDto) {

        Optional<Volunteer> volunteerOpt = userService.findVolunteerById(volunteerId);

        if (volunteerOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Volunteer volunteer = volunteerOpt.get();

        // Find or create the skill
        Skill skill = skillService
                .findByName(skillDto.getName().toLowerCase())
                .orElse(null);

        if (skill == null) {
            skill = new Skill();
            skill.setName(skillDto.getName().toLowerCase());
            skillService.saveSkill(skill);
        }

        // Associate skill with volunteer
        volunteer.getSkills().add(skill);
        userService.saveUser(volunteer);

        return new ResponseEntity<>(volunteer, HttpStatus.OK);
    }

    /**
     * Remove a skill from a volunteer.
     */
    @DeleteMapping("/volunteers/{volunteerId}/skills/{skillId}")
    @Transactional
    public ResponseEntity<Volunteer> removeSkillFromVolunteer(
            @PathVariable Long volunteerId,
            @PathVariable Long skillId) {

        Optional<Volunteer> volunteerOpt = userService.findVolunteerById(volunteerId);

        if (volunteerOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Volunteer volunteer = volunteerOpt.get();
        Skill skill = skillService.findById(skillId).orElse(null);

        // Validate skill association
        if (skill == null || !volunteer.getSkills().contains(skill)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        volunteer.getSkills().remove(skill);
        userService.saveUser(volunteer);

        return new ResponseEntity<>(volunteer, HttpStatus.OK);
    }

    // ---------------- Task Skill APIs ----------------

    /**
     * Retrieve all skills required for a task.
     */
    @GetMapping("/tasks/{taskId}/skills")
    public ResponseEntity<List<Skill>> getTaskSkills(@PathVariable Long taskId) {

        Optional<Task> taskOpt = taskService.findById(taskId);

        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Skill> skills = new ArrayList<>(taskOpt.get().getSkills());
        return new ResponseEntity<>(skills, HttpStatus.OK);
    }

    /**
     * Add a skill to a task.
     * Skill is created if it does not already exist.
     */
    @PostMapping("/tasks/{taskId}/skills")
    @Transactional
    public ResponseEntity<Task> addSkillToTask(
            @PathVariable Long taskId,
            @RequestBody @Valid SkillDto skillDto) {

        Optional<Task> taskOpt = taskService.findById(taskId);

        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Task task = taskOpt.get();

        // Find or create skill
        Skill skill = skillService
                .findByName(skillDto.getName().toLowerCase())
                .orElse(null);

        if (skill == null) {
            skill = new Skill();
            skill.setName(skillDto.getName().toLowerCase());
            skillService.saveSkill(skill);
        }

        task.getSkills().add(skill);
        taskService.saveTask(task);

        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    /**
     * Remove a skill from a task.
     */
    @DeleteMapping("/tasks/{taskId}/skills/{skillId}")
    @Transactional
    public ResponseEntity<Task> removeSkillFromTask(
            @PathVariable Long taskId,
            @PathVariable Long skillId) {

        Optional<Task> taskOpt = taskService.findById(taskId);

        if (taskOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Task task = taskOpt.get();
        Skill skill = skillService.findById(skillId).orElse(null);

        // Validate skill association
        if (skill == null || !task.getSkills().contains(skill)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        task.getSkills().remove(skill);
        taskService.saveTask(task);

        return new ResponseEntity<>(task, HttpStatus.OK);
    }
}
