package com.scaffold.exception;

/** Metama, kai registracijos metu vartotojo vardas jau egzistuoja DB. → HTTP 409 */
public class UsernameTakenException extends DomainException {
    public UsernameTakenException(String username) {
        super("Toks vartotojo vardas jau užimtas: " + username);
    }
}
