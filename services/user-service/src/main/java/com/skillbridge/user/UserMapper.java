package com.skillbridge.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static User toUser(UserRequest userRequest) {
        if (userRequest == null) {
            return null;
        }
        return User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .email(userRequest.email())
                .dateOfBirth(userRequest.dateOfBirth())
                .phoneNumber(userRequest.phoneNumber())
                .build();
    }

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getPhoneNumber());
    }

}
