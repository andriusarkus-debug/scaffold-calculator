package com.scaffold.dto;

import lombok.Data;

// POST /api/auth/login
// {
//   "username": "jonas",
//   "password": "slaptazodis123"
// }
@Data
public class LoginRequest {
    private String username; // Vartotojo vardas prisijungimui
    private String password; // Slaptažodis prisijungimui
}
