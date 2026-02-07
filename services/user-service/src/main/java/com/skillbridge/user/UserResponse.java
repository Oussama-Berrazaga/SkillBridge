package com.skillbridge.user;

import java.sql.Date;

public record UserResponse(String firstName,
        String lastName,
        String email,
        Date dateOfBirth,
        String phoneNumber) {

}
