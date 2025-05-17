package com.baybaka.mikrotikcontrol.presentation.web

import com.baybaka.mikrotikcontrol.application.service.RuleManagementService
import com.baybaka.mikrotikcontrol.application.service.SchedulerService
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.http.ResponseEntity

/**
 * Контроллер для веб-интерфейса управления правилами
 */
@Controller
class RuleWebController(
    private val ruleManagementService: RuleManagementService,
    private val schedulerService: SchedulerService
) {
    
    /**
     * Показать главную страницу со списком правил
     */
    @GetMapping("/")
    fun index(model: Model): String {
        val rules = ruleManagementService.getAllRules()
        model.addAttribute("rules", rules)
        model.addAttribute("types", MikrotikRule.RuleType.values())
        return "index"
    }
    
    /**
     * Переключить состояние правила (для AJAX запросов с фронта)
     */
    @PostMapping("/toggle")
    fun toggleRule(
        @RequestParam uid: String,
        @RequestParam enable: Boolean
    ): ResponseEntity<Map<String, Any>> {
        return try {
            // Отменяем все запланированные задачи
            schedulerService.cancelTasksForRule(uid)
            
            val rule = ruleManagementService.toggleRule(uid, enable)
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "rule" to rule,
                "message" to "Правило успешно ${if (enable) "включено" else "отключено"}"
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "error" to "Ошибка: ${e.message}"
            ))
        }
    }
    
    /**
     * Запланировать изменение состояния правила (для AJAX запросов с фронта)
     */
    @PostMapping("/schedule")
    fun scheduleRuleStateChange(
        @RequestParam uid: String,
        @RequestParam targetState: Boolean,
        @RequestParam minutes: Int
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val task = schedulerService.scheduleRuleStateChange(uid, targetState, minutes)
            
            val action = if (targetState) "включение" else "отключение"
            ResponseEntity.ok(mapOf(
                "success" to true,
                "taskId" to task.id,
                "message" to "Запланировано $action правила через $minutes минут"
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "error" to "Ошибка: ${e.message}"
            ))
        }
    }
    
    /**
     * Страница для отображения запланированных задач
     */
    @GetMapping("/scheduled-tasks")
    fun scheduledTasks(model: Model): String {
        val tasks = schedulerService.getAllActiveTasks()
        val taskDtos = tasks.map { task ->
            val rule = ruleManagementService.getRuleState(task.ruleUid)
            val now = java.time.Instant.now()
            val remainingMinutes = if (task.executionTime.isAfter(now)) {
                java.time.Duration.between(now, task.executionTime).toMinutes()
            } else {
                0L
            }
            mapOf(
                "id" to task.id,
                "ruleUid" to task.ruleUid,
                "ruleDescription" to rule.description,
                "targetState" to task.targetState,
                "executionTime" to task.executionTime,
                "remainingMinutes" to remainingMinutes
            )
        }
        
        model.addAttribute("tasks", taskDtos)
        return "scheduled-tasks"
    }
}