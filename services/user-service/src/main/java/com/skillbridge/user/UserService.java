package com.skillbridge.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.exception.UserNotFoundException;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUserById(@NonNull Long id) {
        return userMapper
                .fromUser(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    public UserResponse createUser(UserRequest userRequest) {
        User user = userMapper.toUser(userRequest);
        if (user == null) {
            throw new IllegalArgumentException("Failed to create user from request");
        }
        return userMapper.fromUser(userRepository.save(user));
    }

    public UserResponse updateUser(@NonNull Long id, UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        userMapper.updateUserFromRequest(userRequest, user);
        return userMapper.fromUser(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::fromUser)
                .toList();
    }

    public void deleteUser(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}
