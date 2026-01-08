package com.example.volunteer_platform.controller;

import com.example.volunteer_platform.dto.OrganizationDto;
import com.example.volunteer_platform.dto.VolunteerDto;
import com.example.volunteer_platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for authentication and user registration.
 */
@Controller
@RequestMapping("/register")
public class AuthController {

    private final UserService userService;

    // Constructor injection (recommended)
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the volunteer registration form.
     */
    @GetMapping("/volunteer")
    public String volunteerRegister(Model model) {
        model.addAttribute("volunteerDto", new VolunteerDto());
        return "volunteer_registration";
    }

    /**
     * Handles volunteer registration.
     */
    @PostMapping("/volunteer")
    public String registerVolunteer(
            @Valid @ModelAttribute("volunteerDto") VolunteerDto volunteerDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "volunteer_registration";
        }

        userService.saveVolunteer(volunteerDto);
        return "redirect:/login";
    }

    /**
     * Displays the organization registration form.
     */
    @GetMapping("/organization")
    public String organizationRegister(Model model) {
        model.addAttribute("organizationDto", new OrganizationDto());
        return "organization_registration";
    }

    /**
     * Handles organization registration.
     */
    @PostMapping("/organization")
    public String registerOrganization(
            @Valid @ModelAttribute("organizationDto") OrganizationDto organizationDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "organization_registration";
        }

        userService.saveOrganization(organizationDto);
        return "redirect:/login";
    }
}
