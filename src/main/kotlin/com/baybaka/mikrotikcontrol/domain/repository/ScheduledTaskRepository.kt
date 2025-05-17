package com.baybaka.mikrotikcontrol.domain.repository

import com.baybaka.mikrotikcontrol.domain.model.ScheduledTask
import java.time.Instant

/**
 * Репозиторий для работы с запланированными задачами
 */
interface ScheduledTaskRepository {
    fun save(task: ScheduledTask): ScheduledTask
    fun findById(id: String): ScheduledTask?
    fun findAll(): List<ScheduledTask>
    fun findByRuleUid(ruleUid: String): List<ScheduledTask>
    fun findActiveTasksDueBefore(time: Instant): List<ScheduledTask>
    fun deleteById(id: String)
    fun deleteByRuleUid(ruleUid: String)
    fun markAsCompleted(id: String)
}
