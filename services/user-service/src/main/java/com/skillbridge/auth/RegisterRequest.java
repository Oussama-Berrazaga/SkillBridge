package com.skillbridge.auth;

import com.skillbridge.user.Role;
import lombok.Data;

@Data
public class RegisterRequest {
  private String email;
  private String password;
  private Role role;
}