package com.skillbridge.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.exception.UserNotFoundException;

import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserById(@NonNull Long id) {
        return UserMapper
                .fromUser(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    public UserResponse createUser(UserRequest userRequest) {
        User user = UserMapper.toUser(userRequest);
        if (user == null) {
            throw new IllegalArgumentException("Failed to create user from request");
        }
        return UserMapper.fromUser(userRepository.save(user));
    }

    public UserResponse updateUser(@NonNull Long id, UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        updateUserFromRequest(userRequest, user);
        return UserMapper.fromUser(userRepository.save(user));
    }

    public static void updateUserFromRequest(UserRequest userRequest, User user) {
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
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::fromUser)
                .toList();
    }
}
