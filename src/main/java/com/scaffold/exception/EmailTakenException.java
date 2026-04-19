package com.scaffold.exception;

/** Metama, kai registracijos metu el. paštas jau egzistuoja DB. → HTTP 409 */
public class EmailTakenException extends DomainException {
    public EmailTakenException(String email) {
        super("Šis el. paštas jau naudojamas: " + email);
    }
}
