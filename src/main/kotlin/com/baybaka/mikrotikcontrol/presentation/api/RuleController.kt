package com.baybaka.mikrotikcontrol.presentation.api

import com.baybaka.mikrotikcontrol.application.service.RuleManagementService
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.presentation.dto.BulkToggleRequest
import com.baybaka.mikrotikcontrol.presentation.dto.MikrotikRuleDto
import com.baybaka.mikrotikcontrol.presentation.dto.toDto
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger
import kotlin.math.log

/**
 * REST контроллер для управления правилами MikroTik
 */
@RestController
@RequestMapping("/api/rules")

class RuleController(private val ruleManagementService: RuleManagementService) {
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
}
