package com.xtensus.hrmanagementapi.common.exception;

import com.xtensus.hrmanagementapi.department.exception.DepartmentNotFoundException;
import com.xtensus.hrmanagementapi.department.exception.DuplicateDepartmentException;
import com.xtensus.hrmanagementapi.auth.exception.AccountDisabledException;
import com.xtensus.hrmanagementapi.auth.exception.AccountInactiveException;
import com.xtensus.hrmanagementapi.auth.exception.InvalidCredentialsException;
import com.xtensus.hrmanagementapi.leave.balance.exception.DuplicateLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.InsufficientLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.InvalidLeaveBalanceException;
import com.xtensus.hrmanagementapi.leave.balance.exception.LeaveBalanceNotFoundException;
import com.xtensus.hrmanagementapi.leave.request.exception.InvalidLeaveRequestException;
import com.xtensus.hrmanagementapi.leave.request.exception.LeaveDecisionNotAllowedException;
import com.xtensus.hrmanagementapi.leave.request.exception.LeaveRequestNotFoundException;
import com.xtensus.hrmanagementapi.leave.request.exception.UnauthorizedApproverException;
import com.xtensus.hrmanagementapi.leave.request.exception.UpdateNotAllowedException;
import com.xtensus.hrmanagementapi.leave.type.exception.DuplicateLeaveTypeException;
import com.xtensus.hrmanagementapi.leave.type.exception.LeaveTypeNotFoundException;
import com.xtensus.hrmanagementapi.medical.document.exception.DuplicateMedicalDocumentException;
import com.xtensus.hrmanagementapi.medical.document.exception.InvalidMedicalDocumentException;
import com.xtensus.hrmanagementapi.medical.document.exception.MedicalDocumentNotFoundException;
import com.xtensus.hrmanagementapi.medical.document.exception.StorageException;
import com.xtensus.hrmanagementapi.notification.exception.NotificationNotFoundException;
import com.xtensus.hrmanagementapi.position.exception.DuplicatePositionException;
import com.xtensus.hrmanagementapi.position.exception.PositionNotFoundException;
import com.xtensus.hrmanagementapi.user.exception.DuplicateEmailException;
import com.xtensus.hrmanagementapi.user.exception.DuplicateUsernameException;
import com.xtensus.hrmanagementapi.user.exception.InvalidManagerException;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, null);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountDisabled(
            AccountDisabledException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request, null);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountInactive(
            AccountInactiveException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentNotFound(
            DepartmentNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateDepartmentException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateDepartment(
            DuplicateDepartmentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(PositionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePositionNotFound(
            PositionNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicatePositionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicatePosition(
            DuplicatePositionException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(LeaveTypeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveTypeNotFound(
            LeaveTypeNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateLeaveTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateLeaveType(
            DuplicateLeaveTypeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(LeaveBalanceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveBalanceNotFound(
            LeaveBalanceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateLeaveBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateLeaveBalance(
            DuplicateLeaveBalanceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientLeaveBalance(
            InsufficientLeaveBalanceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidLeaveBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLeaveBalance(
            InvalidLeaveBalanceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveRequestNotFound(
            LeaveRequestNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidLeaveRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLeaveRequest(
            InvalidLeaveRequestException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(UpdateNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleUpdateNotAllowed(
            UpdateNotAllowedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(LeaveDecisionNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveDecisionNotAllowed(
            LeaveDecisionNotAllowedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(UnauthorizedApproverException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedApprover(
            UnauthorizedApproverException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request, null);
    }

    @ExceptionHandler(MedicalDocumentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMedicalDocumentNotFound(
            MedicalDocumentNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateMedicalDocumentException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateMedicalDocument(
            DuplicateMedicalDocumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidMedicalDocumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidMedicalDocument(
            InvalidMedicalDocumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageException(
            StorageException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request, null);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFound(
            NotificationNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUsername(
            DuplicateUsernameException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidManagerException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidManager(
            InvalidManagerException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                validationErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed or invalid request body",
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = "Invalid value for parameter '" + exception.getName() + "'";
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Request conflicts with existing data",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
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
