package com.baybaka.mikrotikcontrol.presentation.api

import com.baybaka.mikrotikcontrol.domain.exception.DomainException
import com.baybaka.mikrotikcontrol.domain.exception.EntityNotFoundException
import com.baybaka.mikrotikcontrol.domain.exception.MikrotikApiException
import com.baybaka.mikrotikcontrol.domain.exception.RuleDescriptionMismatchException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * Глобальный обработчик исключений для REST API
 */
@RestControllerAdvice(basePackages = ["com.baybaka.mikrotikcontrol.presentation.api"])
class ApiExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
    
    /**
     * DTO для передачи ошибок клиенту
     */
    data class ErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Обработка исключения "сущность не найдена"
     */
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(ex: EntityNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Entity not found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message ?: "Сущность не найдена"))
    }
    
    /**
     * Обработка исключения несоответствия описания
     */
    @ExceptionHandler(RuleDescriptionMismatchException::class)
    fun handleDescriptionMismatch(ex: RuleDescriptionMismatchException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Rule description mismatch: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), ex.message ?: "Описание правила не совпадает"))
    }
    
    /**
     * Обработка исключений API Mikrotik
     */
    @ExceptionHandler(MikrotikApiException::class)
    fun handleMikrotikApiException(ex: MikrotikApiException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Mikrotik API error: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Ошибка API MikroTik: ${ex.message}"))
    }
    
    /**
     * Обработка остальных доменных исключений
     */
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Domain error: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message ?: "Внутренняя ошибка домена"))
    }
    
    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Непредвиденная ошибка: ${ex.message}"))
    }
}
