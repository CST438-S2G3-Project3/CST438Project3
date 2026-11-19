package com.example.trip_advisor_api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import java.net.URI

/** RFC 9457 responses scoped to destination endpoints, preserving other controllers' behavior. */
@RestControllerAdvice(assignableTypes = [DestinationController::class])
class DestinationErrorHandler {
    /** Converts expected domain failures to a safe, consistent response. */
    @ExceptionHandler(ResponseStatusException::class)
    fun domain(error: ResponseStatusException, request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val body = ProblemDetail.forStatusAndDetail(error.statusCode, error.reason ?: "Request failed")
        body.type = URI.create("about:blank")
        body.instance = URI.create(request.requestURI)
        val response = ResponseEntity.status(error.statusCode).contentType(MediaType.APPLICATION_PROBLEM_JSON)
        if (error.statusCode.value() == 401) {
            response.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
        }
        return response.body(body)
    }

    /** Rejects malformed JSON and path identifiers without exposing parser internals. */
    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun malformed(error: Exception, request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val detail = if (error is MethodArgumentTypeMismatchException) {
            "Trip and destination IDs must be valid UUIDs"
        } else {
            "Provide a JSON object with name and location"
        }
        val body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail)
        body.type = URI.create("about:blank")
        body.instance = URI.create(request.requestURI)
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body)
    }
}
