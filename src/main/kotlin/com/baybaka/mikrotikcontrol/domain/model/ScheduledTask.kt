package com.baybaka.mikrotikcontrol.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Запланированная задача для переключения состояния правила в определенное время
 */
data class ScheduledTask(
    val id: String = UUID.randomUUID().toString(),
    val ruleUid: String,              // ID правила
    val targetState: Boolean,         // Целевое состояние (true = включить, false = выключить)
    val executionTime: Instant,       // Время выполнения
    val created: Instant = Instant.now(), // Время создания задачи
    var completed: Boolean = false    // Флаг выполнения задачи
)
