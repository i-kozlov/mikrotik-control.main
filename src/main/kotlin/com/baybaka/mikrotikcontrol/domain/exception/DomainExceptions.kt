package com.baybaka.mikrotikcontrol.domain.exception

/**
 * Базовое исключение домена
 */
open class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Исключение, когда сущность не найдена
 */
class EntityNotFoundException(message: String) : DomainException(message)

/**
 * Исключение, когда описание правила на сервере не соответствует ожидаемому
 */
class RuleDescriptionMismatchException(message: String) : DomainException(message)

/**
 * Исключение при коммуникации с Mikrotik API
 */
class MikrotikApiException(message: String, cause: Throwable? = null) : DomainException(message, cause)
