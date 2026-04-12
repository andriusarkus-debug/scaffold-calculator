package com.scaffold.dto;

import lombok.Data;

// DTO = Duomenų perdavimo objektas — JSON struktūra, kurią laukiame iš kliento
// POST /api/auth/register
// {
//   "username": "jonas",
//   "email": "jonas@pavyzdys.lt",
//   "password": "slaptazodis123"
// }
@Data
public class RegisterRequest {
    private String username; // Norimas vartotojo vardas
    private String email; // Vartotojo el. pašto adresas
    private String password; // Vartotojo slaptažodis (dar nešifruotas)
}
