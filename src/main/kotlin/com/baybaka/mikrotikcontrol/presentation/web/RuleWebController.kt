package com.baybaka.mikrotikcontrol.presentation.web

import com.baybaka.mikrotikcontrol.application.service.RuleManagementService
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
class RuleWebController(private val ruleManagementService: RuleManagementService) {
    
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
}