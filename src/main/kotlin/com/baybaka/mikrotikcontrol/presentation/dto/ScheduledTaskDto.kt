package com.baybaka.mikrotikcontrol.presentation.dto

import com.baybaka.mikrotikcontrol.domain.model.ScheduledTask
import java.time.Instant

/**
 * DTO для запланированных задач
 */
data class ScheduledTaskDto(
    val id: String,
    val ruleUid: String,
    val ruleDescription: String?,    // Добавляем описание для удобства отображения
    val targetState: Boolean,
    val executionTime: Instant,
    val created: Instant,
    val completed: Boolean,
    val remainingMinutes: Long?      // Оставшееся время в минутах
)

/**
 * Конвертация из доменной модели в DTO
 */
fun ScheduledTask.toDto(ruleDescription: String? = null, remainingMinutes: Long? = null): ScheduledTaskDto {
    return ScheduledTaskDto(
        id = this.id,
        ruleUid = this.ruleUid,
        ruleDescription = ruleDescription,
        targetState = this.targetState,
        executionTime = this.executionTime,
        created = this.created,
        completed = this.completed,
        remainingMinutes = remainingMinutes
    )
}
