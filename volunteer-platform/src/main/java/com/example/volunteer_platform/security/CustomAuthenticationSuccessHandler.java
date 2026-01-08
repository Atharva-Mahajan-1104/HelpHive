package com.example.volunteer_platform.security;

import com.example.volunteer_platform.model.Organization;
import com.example.volunteer_platform.model.Skill;
import com.example.volunteer_platform.model.Volunteer;
import com.example.volunteer_platform.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Custom authentication success handler.
 * 
 * This class is executed immediately after a user successfully logs in.
 * It is responsible for:
 * - Storing user-related details in the HTTP session
 * - Determining the user role
 * - Redirecting users based on their role (Organization / Volunteer)
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // Service used to fetch user details from the database
    private final UserService userService;

    public CustomAuthenticationSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method is triggered after successful authentication.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // Extract the authenticated username (email)
        String username = authentication.getName();

        // Store username in session for later use
        request.getSession().setAttribute("user", username);

        // Extract user role from granted authorities
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        // Store role in session
        request.getSession().setAttribute("role", role);

        // Fetch the user ID using email
        Long id = userService.findByEmail(username).getId();

        /**
         * Handle ORGANIZATION user session setup
         */
        if ("ROLE_ORGANIZATION".equals(role) && id != null) {

            Optional<Organization> userObj = userService.findOrganizationById(id);

            if (userObj.isPresent()) {
                Organization org = userObj.get();

                // Store organization-specific attributes in session
                request.getSession().setAttribute("userId", org.getId());
                request.getSession().setAttribute("name", org.getName());
                request.getSession().setAttribute("email", org.getEmail());
                request.getSession().setAttribute("phone", org.getPhoneNumber());
                request.getSession().setAttribute("address", org.getAddress());
                request.getSession().setAttribute("website", org.getWebsite());

                // Fields not applicable for organizations
                request.getSession().setAttribute("gender", null);
                request.getSession().setAttribute("skills", null);
                request.getSession().setAttribute("skillNames", null);

                // Organization-related data
                request.getSession().setAttribute("tasks", org.getTasks());
                request.getSession().setAttribute("createdAt", org.getCreatedAt());
                request.getSession().setAttribute("updatedAt", org.getUpdatedAt());
            }
        }

        /**
         * Handle VOLUNTEER user session setup
         */
        else if ("ROLE_VOLUNTEER".equals(role) && id != null) {

            Optional<Volunteer> userObj = userService.findVolunteerById(id);

            if (userObj.isPresent()) {
                Volunteer volunteer = userObj.get();

                // Store volunteer-specific attributes in session
                request.getSession().setAttribute("userId", volunteer.getId());
                request.getSession().setAttribute("name", volunteer.getName());
                request.getSession().setAttribute("email", volunteer.getEmail());
                request.getSession().setAttribute("phone", volunteer.getPhoneNumber());

                // Fields not applicable for volunteers
                request.getSession().setAttribute("address", null);
                request.getSession().setAttribute("website", null);

                request.getSession().setAttribute("gender", volunteer.getGender());
                request.getSession().setAttribute("skills", volunteer.getSkills());

                // Convert skill list to comma-separated string
                String skillNames = volunteer.getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.joining(","));

                request.getSession().setAttribute("skillNames", skillNames);
                request.getSession().setAttribute("createdAt", volunteer.getCreatedAt());
                request.getSession().setAttribute("updatedAt", volunteer.getUpdatedAt());
            }
        }

        /**
         * Determine redirect URL based on user role
         */
        String redirectUrl;

        if ("ROLE_ORGANIZATION".equals(role)) {
            redirectUrl = "/o/current_tasks";
        } 
        else if ("ROLE_VOLUNTEER".equals(role)) {
            redirectUrl = "/v/opportunities";
        } 
        else {
            redirectUrl = "/home";
        }

        // Redirect user after successful login
        response.sendRedirect(redirectUrl);
    }
}
