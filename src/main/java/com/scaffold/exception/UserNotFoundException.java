package com.scaffold.exception;

/** Metama, kai ieškomo vartotojo nėra DB. → HTTP 404 */
public class UserNotFoundException extends DomainException {
    public UserNotFoundException(Long id) {
        super("Vartotojas nerastas: ID " + id);
    }

    public UserNotFoundException(String username) {
        super("Vartotojas nerastas: " + username);
    }
}
