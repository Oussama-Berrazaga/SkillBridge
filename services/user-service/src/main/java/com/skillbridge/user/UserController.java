package com.skillbridge.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    public ResponseEntity<UserResponse> getUserById(@NotNull Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    public ResponseEntity<UserResponse> updateUser(Long id, @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }
}
