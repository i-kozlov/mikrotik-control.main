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
import java.util.LinkedHashMap

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
        
        // Группируем правила по группам, сохраняя порядок из конфигурации
        val groupedRules = rules.groupBy { it.group }
        
        // Получаем отсортированные группы из конфигурации
        val sortedGroupConfigs = ruleManagementService.getSortedGroupConfigs()
        
        // Создаем упорядоченную карту групп
        val orderedGroupedRules = LinkedHashMap<String, List<MikrotikRule>>()
        
        // Сначала добавляем группы в порядке конфигурации
        for ((groupKey, groupConfig) in sortedGroupConfigs) {
            groupedRules[groupKey]?.let { rulesInGroup ->
                orderedGroupedRules[groupKey] = rulesInGroup
            }
        }
        
        // Добавляем группы, которых нет в конфигурации (в алфавитном порядке)
        val configuredGroupKeys = sortedGroupConfigs.map { it.first }.toSet()
        val unconfiguredGroups = groupedRules.keys.minus(configuredGroupKeys).sorted()
        for (groupKey in unconfiguredGroups) {
            orderedGroupedRules[groupKey] = groupedRules[groupKey]!!
        }
        
        // Получаем все уникальные группы в правильном порядке
        val allGroups = orderedGroupedRules.keys.toList()
        
        model.addAttribute("rules", rules)
        model.addAttribute("groupedRules", orderedGroupedRules)
        model.addAttribute("allGroups", allGroups)
        model.addAttribute("groupConfigs", sortedGroupConfigs.associate { it.first to it.second })
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