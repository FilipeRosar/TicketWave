package com.projeto.ticket_wave.shared.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ApiErrorController implements ErrorController {

    @RequestMapping("${server.error.path:/error}")
    ResponseEntity<ApiError> error(HttpServletRequest request) {
        Object dispatchedStatus = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatusCode status = dispatchedStatus instanceof Integer code
                ? HttpStatusCode.valueOf(code) : HttpStatus.NOT_FOUND;
        Object dispatchedPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = dispatchedPath instanceof String originalPath ? originalPath : request.getRequestURI();
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(status, ErrorMessages.forStatus(status), path));
    }
}
