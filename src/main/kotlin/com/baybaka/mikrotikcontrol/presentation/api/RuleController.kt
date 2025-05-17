package com.baybaka.mikrotikcontrol.presentation.api

import com.baybaka.mikrotikcontrol.application.service.RuleManagementService
import com.baybaka.mikrotikcontrol.application.service.SchedulerService
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.presentation.dto.BulkToggleRequest
import com.baybaka.mikrotikcontrol.presentation.dto.MikrotikRuleDto
import com.baybaka.mikrotikcontrol.presentation.dto.ScheduledTaskDto
import com.baybaka.mikrotikcontrol.presentation.dto.toDto
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.Instant

/**
 * REST контроллер для управления правилами MikroTik
 */
@RestController
@RequestMapping("/api/rules")
class RuleController(
    private val ruleManagementService: RuleManagementService,
    private val schedulerService: SchedulerService
) {
    private val logger = LoggerFactory.getLogger(RuleController::class.java)

    /**
     * Получить все правила
     */
    @GetMapping
    fun getAllRules(): ResponseEntity<List<MikrotikRuleDto>> {
        val rules = ruleManagementService.getAllRules()
        return ResponseEntity.ok(rules.map { it.toDto() })
    }
    
    /**
     * Получить правило по UID
     */
    @GetMapping("/{uid}")
    fun getRuleState(@PathVariable uid: String): ResponseEntity<MikrotikRuleDto> {
        val rule = ruleManagementService.getRuleState(uid)
        return ResponseEntity.ok(rule.toDto())
    }
    
    /**
     * Переключить состояние правила
     */
    @PostMapping("/{uid}/toggle")
    fun toggleRule(
        @PathVariable uid: String,
        @RequestParam(defaultValue = "true") enable: Boolean
    ): ResponseEntity<MikrotikRuleDto> {
        logger.info("Переключение состояния правила $uid на $enable")
        
        // Отменяем все запланированные задачи для этого правила
        schedulerService.cancelTasksForRule(uid)
        
        val rule = ruleManagementService.toggleRule(uid, enable)
        return ResponseEntity.ok(rule.toDto())
    }
    
    /**
     * Массовое переключение правил
     */
    @PostMapping("/toggle-bulk")
    fun toggleBulkRules(@RequestBody request: BulkToggleRequest): ResponseEntity<List<MikrotikRuleDto>> {
        val rules = ruleManagementService.toggleRules(request.ruleIds, request.enable)
        return ResponseEntity.ok(rules.map { it.toDto() })
    }
    
    /**
     * Запланировать изменение состояния правила
     */
    @PostMapping("/{uid}/schedule")
    fun scheduleRuleStateChange(
        @PathVariable uid: String,
        @RequestParam targetState: Boolean,
        @RequestParam minutes: Int
    ): ResponseEntity<Map<String, Any>> {
        try {
            val task = schedulerService.scheduleRuleStateChange(uid, targetState, minutes)
            
            val action = if (targetState) "включение" else "отключение"
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "taskId" to task.id,
                "message" to "Запланировано $action правила через $minutes минут"
            ))
        } catch (e: Exception) {
            return ResponseEntity.ok(mapOf(
                "success" to false,
                "error" to "Ошибка: ${e.message}"
            ))
        }
    }
    
    /**
     * Получить список запланированных задач для правила
     */
    @GetMapping("/{uid}/scheduled-tasks")
    fun getScheduledTasks(@PathVariable uid: String): ResponseEntity<List<ScheduledTaskDto>> {
        val rule = ruleManagementService.getRuleState(uid)
        val tasks = schedulerService.getAllActiveTasks()
            .filter { it.ruleUid == uid }
            .map { 
                val now = Instant.now()
                val remainingMinutes = if (it.executionTime.isAfter(now)) {
                    Duration.between(now, it.executionTime).toMinutes()
                } else {
                    0L
                }
                it.toDto(rule.description, remainingMinutes)
            }
        
        return ResponseEntity.ok(tasks)
    }
    
    /**
     * Отменить запланированные задачи для правила
     */
    @DeleteMapping("/{uid}/scheduled-tasks")
    fun cancelScheduledTasks(@PathVariable uid: String): ResponseEntity<Map<String, Any>> {
        schedulerService.cancelTasksForRule(uid)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Запланированные задачи отменены"))
    }
}
