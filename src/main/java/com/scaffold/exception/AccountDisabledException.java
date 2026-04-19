package com.scaffold.exception;

/** Metama, kai prisijungiama prie išjungtos paskyros. → HTTP 403 */
public class AccountDisabledException extends DomainException {
    public AccountDisabledException() {
        super("Paskyra išjungta. Susisiekite su administratoriumi.");
    }
}
