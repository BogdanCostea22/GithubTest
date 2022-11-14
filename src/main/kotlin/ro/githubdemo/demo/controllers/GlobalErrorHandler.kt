package ro.githubdemo.demo.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalErrorHandler {

    @ExceptionHandler(CustomAppException::class)
    suspend fun handleExceptions(exc: CustomAppException): ResponseEntity<CustomAppException> {
        return ResponseEntity<CustomAppException>(exc, exc.status)
    }

    @ExceptionHandler(Exception::class)
    suspend fun handleThrownException(exc: Exception): ResponseEntity<CustomAppException> {
        val error = CustomAppException.InternalServerError()
        return ResponseEntity<CustomAppException>(error, error.status)
    }
}