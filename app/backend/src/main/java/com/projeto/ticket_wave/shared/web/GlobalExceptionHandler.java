package com.projeto.ticket_wave.shared.web;

import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.exception.ConflictException;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import com.projeto.ticket_wave.shared.exception.NotFoundException;
import com.projeto.ticket_wave.shared.exception.StorageException;
import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ApiError> concurrentUpdate(HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT, "O recurso foi alterado. Consulte a versão atual e tente novamente.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        if (ex.getMostSpecificCause() instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
            return respond(HttpStatus.CONFLICT, "Já existe um registro com os dados informados.", request);
        }
        return unexpected(ex, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiError> unauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return respond(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> invalidAuthentication(HttpServletRequest request) {
        return respond(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.", request);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> forbidden(ForbiddenException ex, HttpServletRequest request) {
        return respond(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> accessDenied(HttpServletRequest request) {
        return respond(HttpStatus.FORBIDDEN, "Você não possui permissão para acessar este recurso.", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        var violations = ex.getConstraintViolations().stream()
                .map(violation -> new ApiError.Violation(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return response(ApiError.of(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos.", request.getRequestURI(), violations));
    }

    @ExceptionHandler(StorageException.class)
    ResponseEntity<ApiError> storage(StorageException ex, HttpServletRequest request) {
        log.error("Falha de armazenamento em {}", request.getRequestURI(), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao acessar o armazenamento.", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.forStatus(HttpStatus.INTERNAL_SERVER_ERROR), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        List<ApiError.Violation> violations = List.of();
        String message = ErrorMessages.forStatus(status);
        if (ex instanceof BindException binding) {
            message = "Dados de entrada inválidos.";
            violations = binding.getAllErrors().stream()
                    .map(error -> new ApiError.Violation(
                            error instanceof FieldError field ? field.getField() : error.getObjectName(),
                            error instanceof FieldError field && field.isBindingFailure()
                                    ? "Valor inválido." : error.getDefaultMessage()))
                    .toList();
        } else if (ex instanceof HandlerMethodValidationException validation && !validation.isForReturnValue()) {
            message = "Dados de entrada inválidos.";
            violations = validation.getParameterValidationResults().stream()
                    .flatMap(result -> result.getResolvableErrors().stream()
                            .map(error -> new ApiError.Violation(
                                    result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                    .toList();
        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "Corpo da requisição ausente ou JSON inválido.";
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return super.handleExceptionInternal(ex, ApiError.of(status, message, path, violations),
                responseHeaders, status, request);
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String message, HttpServletRequest request) {
        return response(ApiError.of(status, message, request.getRequestURI()));
    }

    private ResponseEntity<ApiError> response(ApiError error) {
        return ResponseEntity.status(error.status()).contentType(MediaType.APPLICATION_JSON).body(error);
    }
}
