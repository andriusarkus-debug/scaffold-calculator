package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AuthResponse { // Autentifikacijos atsakymo objektas
    private String token; // JWT žetonas, kurį klientas naudos sekančiose užklausose
    private String username; // Prisijungusio vartotojo vardas
    private String role; // Prisijungusio vartotojo vaidmuo sistemoje
}
