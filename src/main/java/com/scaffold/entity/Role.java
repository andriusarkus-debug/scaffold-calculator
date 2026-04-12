package com.scaffold.entity;


// Tai yra 3 galimi vartotojo vaidmenys sistemoje.

public enum Role {
    ROLE_USER,      // Paprastas darbuotojas — gali naudoti skaičiuoklę, peržiūrėti savo istoriją
    ROLE_MANAGER,   // Vadovas — gali peržiūrėti visus skaičiavimus, valdyti medžiagas
    ROLE_ADMIN      // Administratorius — pilna prieiga, valdo vartotojus ir vaidmenis
}
