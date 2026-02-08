package com.skillbridge.user;

import org.springframework.stereotype.Component;

import io.micrometer.common.util.StringUtils;

@Component
public class UserMapper {

    public User toUser(UserRequest userRequest) {
        if (userRequest == null) {
            return null;
        }
        return User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .email(userRequest.email())
                .dateOfBirth(userRequest.dateOfBirth())
                .phoneNumber(userRequest.phoneNumber())
                .role(userRequest.role())
                .build();
    }

    public UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getPhoneNumber(),
                user.getRole());
    }

    public void updateUserFromRequest(UserRequest userRequest, User user) {
        if (StringUtils.isNotBlank(userRequest.firstName())) {
            user.setFirstName(userRequest.firstName());
        }
        if (StringUtils.isNotBlank(userRequest.lastName())) {
            user.setLastName(userRequest.lastName());
        }
        if (StringUtils.isNotBlank(userRequest.email())) {
            user.setEmail(userRequest.email());
        }
        if (userRequest.dateOfBirth() != null) {
            user.setDateOfBirth(userRequest.dateOfBirth());
        }
        if (StringUtils.isNotBlank(userRequest.phoneNumber())) {
            user.setPhoneNumber(userRequest.phoneNumber());
        }
        if (StringUtils.isNotBlank(userRequest.role().name())) {
            user.setRole(userRequest.role());
        }
    }
}
