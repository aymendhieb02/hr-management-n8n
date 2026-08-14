package com.xtensus.hrmanagementapi.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException exception, HttpServletRequest request) {
        return buildResponse(statusFor(exception), exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, validationErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Corps de requete malforme ou invalide", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Valeur invalide pour le parametre '" + exception.getName() + "'", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception, HttpServletRequest request) {
        String details = exception.getMostSpecificCause() == null ? "" : exception.getMostSpecificCause().getMessage();
        String message;
        if (details != null && details.toLowerCase().contains("duplicate")) {
            message = "Cette valeur existe deja. Utilisez un libelle ou un identifiant unique.";
        } else if (details != null && details.toLowerCase().contains("cannot be null")) {
            message = "Une donnee obligatoire est absente. Actualisez la page puis reessayez.";
        } else if (details != null && details.toLowerCase().contains("foreign key")) {
            message = "Cette donnee est encore utilisee par un autre element et ne peut pas etre modifiee ou supprimee.";
        } else {
            message = "Impossible d'enregistrer cette modification en raison d'une contrainte de la base de donnees.";
        }
        return buildResponse(HttpStatus.CONFLICT, message, request, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                "Le fichier est trop volumineux. Une photo de profil peut faire au maximum 10 Mo.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue", request, null);
    }

    private HttpStatus statusFor(RuntimeException exception) {
        String name = exception.getClass().getSimpleName();
        if (name.contains("Introuvable") || name.contains("NotFound")) return HttpStatus.NOT_FOUND;
        if (name.contains("ExisteDeja") || name.contains("Duplicate") || name.contains("NonAutorisee")) return HttpStatus.CONFLICT;
        if (name.contains("Invalide") || name.contains("Invalid")) return HttpStatus.BAD_REQUEST;
        if (name.contains("Disabled") || name.contains("Inactive")) return HttpStatus.FORBIDDEN;
        if (name.contains("Credentials")) return HttpStatus.UNAUTHORIZED;
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request, Map<String, String> validationErrors) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(message);
        response.setPath(request.getRequestURI());
        response.setValidationErrors(validationErrors);
        return ResponseEntity.status(status).body(response);
    }
}
