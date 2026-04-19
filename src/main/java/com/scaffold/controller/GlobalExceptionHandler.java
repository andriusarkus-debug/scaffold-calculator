package com.scaffold.controller;

import com.scaffold.exception.AccountDisabledException;
import com.scaffold.exception.DomainException;
import com.scaffold.exception.EmailTakenException;
import com.scaffold.exception.UserNotFoundException;
import com.scaffold.exception.UsernameTakenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gaudo visoje programoje iškylančias išimtis ir grąžina tvarkingus JSON klaidų atsakymus.
 *
 * PASTABA: {@link RestControllerAdvice} veikia tik su {@code @RestController} — t. y. REST API
 * (mobile app, ateities JSON endpoint'ai). Thymeleaf puslapių formos (pvz. AuthController.register)
 * pačios gaudo {@link DomainException} ir parodo pranešimą puslapyje.
 *
 * HTTP statusų parinkimas:
 * <ul>
 *   <li>401 UNAUTHORIZED — neteisingi prisijungimo duomenys</li>
 *   <li>403 FORBIDDEN — paskyra išjungta, nepakankamos teisės</li>
 *   <li>404 NOT_FOUND — resursas nerastas</li>
 *   <li>409 CONFLICT — duomenų konfliktas (užimtas vardas / el. paštas)</li>
 *   <li>500 INTERNAL_SERVER_ERROR — netikėta sistemos klaida</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- 401: neteisingas vardas arba slaptažodis ---
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // --- 403: paskyra išjungta arba trūksta teisių ---
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<Map<String, String>> handleAccountDisabled(AccountDisabledException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, "Neturite teisių atlikti šį veiksmą.");
    }

    // --- 404: resursas nerastas ---
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // --- 409: konfliktas (užimtas vardas / el. paštas) ---
    @ExceptionHandler({UsernameTakenException.class, EmailTakenException.class})
    public ResponseEntity<Map<String, String>> handleConflict(DomainException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    // --- 400: bet kokia kita domeno klaida (fallback mūsų išimtims) ---
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomain(DomainException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // --- 500: netikėta sistemos klaida (NullPointer, DB nutrūkimas ir pan.) ---
    // Svarbu: nerodome vartotojui vidinių klaidos detalių, bet loginame visą stack trace.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        log.error("Netikėta sistemos klaida", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Įvyko netikėta klaida. Bandykite dar kartą.");
    }

    // Pagalbinis metodas — vienoda JSON atsakymo struktūra visoms klaidoms
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
