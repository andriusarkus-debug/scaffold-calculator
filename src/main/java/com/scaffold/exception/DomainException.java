package com.scaffold.exception;

/**
 * Bazinė mūsų domeno klaida — visos projekto specifinės išimtys ją paveldi.
 * Išvengiame tiesioginio {@link RuntimeException} naudojimo, kad
 * {@code GlobalExceptionHandler} galėtų atskirti domeno klaidas nuo
 * netikėtų sistemos klaidų (NullPointer, DB ryšio ir pan.).
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
