package com.skillbridge.domain.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.skillbridge.domain.profile.Profile;
import com.skillbridge.domain.profile.ProfileRequest;
import com.skillbridge.domain.profile.ProfileResponse;
import com.skillbridge.domain.profile.SkillResponse;
import com.skillbridge.domain.profile.TechnicianSkill;

import io.micrometer.common.util.StringUtils;

@Component
public class UserMapper {

    public User toUser(UserRequest request) {
        if (request == null)
            return null;

        // Create the Profile first
        Profile profile = Profile.builder()
                .firstName(request.profile().firstName())
                .lastName(request.profile().lastName())
                .phoneNumber(request.profile().phoneNumber())
                .birthDate(request.profile().birthDate())
                .bio(request.profile().bio())
                .skills(new ArrayList<>())
                .build();

        // Handle Technician Skills if they exist
        if (request.profile().skills() != null) {
            request.profile().skills().forEach(skillReq -> {
                TechnicianSkill skill = TechnicianSkill.builder()
                        .categoryId(skillReq.categoryId())
                        .yearsExperience(skillReq.yearsExperience())
                        .profile(profile) // Critical: Set the back-reference
                        .build();
                profile.getSkills().add(skill);
            });
        }

        // Build the User and link the profile
        return User.builder()
                .email(request.email())
                .password(request.password()) // Should be encoded in Service layer
                .role(request.role())
                .profile(profile)
                .build();
    }

    public UserResponse fromUser(User user) {
        if (user == null)
            return null;

        Profile profile = user.getProfile();

        // Map nested skills to DTOs
        List<SkillResponse> skillResponses = profile.getSkills().stream()
                .map(s -> new SkillResponse(s.getCategoryId(), s.getCategoryName(), s.getYearsExperience()))
                .toList();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                new ProfileResponse(
                        profile.getFirstName(),
                        profile.getLastName(),
                        profile.getPhoneNumber(),
                        profile.getBirthDate(),
                        profile.getBio(),
                        profile.getAverageRating(),
                        skillResponses));
    }

    public void updateUserFromRequest(UserRequest request, User user) {
        if (request == null)
            return;

        // 1. Update User Identity
        if (StringUtils.isNotBlank(request.email())) {
            user.setEmail(request.email());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }

        // 2. Update Profile nested data
        if (request.profile() != null) {
            Profile profile = user.getProfile();
            ProfileRequest pReq = request.profile();

            if (StringUtils.isNotBlank(pReq.firstName()))
                profile.setFirstName(pReq.firstName());
            if (StringUtils.isNotBlank(pReq.lastName()))
                profile.setLastName(pReq.lastName());
            if (StringUtils.isNotBlank(pReq.phoneNumber()))
                profile.setPhoneNumber(pReq.phoneNumber());
            if (pReq.birthDate() != null)
                profile.setBirthDate(pReq.birthDate());
            if (pReq.bio() != null)
                profile.setBio(pReq.bio());

            // 3. Update Skills (Orphan Removal pattern)
            if (pReq.skills() != null) {
                profile.getSkills().clear(); // Clear existing
                pReq.skills().forEach(sReq -> {
                    profile.addSkill(TechnicianSkill.builder()
                            .categoryId(sReq.categoryId())
                            .yearsExperience(sReq.yearsExperience())
                            .build());
                });
            }
        }
    }
}
