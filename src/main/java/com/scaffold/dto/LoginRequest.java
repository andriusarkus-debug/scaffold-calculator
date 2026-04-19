package com.scaffold.dto;

import lombok.Data;


@Data
public class LoginRequest {
    private String username; // Vartotojo vardas prisijungimui
    private String password; // Slaptažodis prisijungimui
}
