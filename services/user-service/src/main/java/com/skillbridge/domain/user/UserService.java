package com.skillbridge.domain.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.exception.UserAlreadyExistsException;
import com.skillbridge.exception.UserNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true) // Default to read-only, override for write operations
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional // Override for write operations
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new UserAlreadyExistsException(
                    String.format("User with email %s already exists", userRequest.email()));
        }

        User user = userMapper.toUser(userRequest);

        // Link the profile back to the user explicitly if mapper didn't
        if (user.getProfile() != null) {
            user.getProfile().setUser(user);
        }

        return userMapper.fromUser(userRepository.save(user));
    }

    public UserResponse getUserById(@NonNull Long id) {
        return userRepository.findById(id)
                .map(userMapper::fromUser)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userMapper.updateUserFromRequest(userRequest, user);
        // Repository.save is optional here due to @Transactional dirty checking,
        // but keeping it is fine for clarity.
        return userMapper.fromUser(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::fromUser)
                .toList();
    }

    public void deleteUser(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
