package com.baybaka.mikrotikcontrol.presentation.dto

import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule.RuleType

/**
 * DTO для передачи информации о правиле MikroTik
 */
data class MikrotikRuleDto(
    val uid: String,
    val type: String,
    val ruleNumber: String,
    val description: String,
    val enabled: Boolean,
    val autoOff: Boolean,
    val autoOn: Boolean
)

/**
 * Запрос на массовое переключение правил
 */
data class BulkToggleRequest(
    val ruleIds: List<String>,
    val enable: Boolean
)

/**
 * Конвертирует доменную модель в DTO
 */
fun MikrotikRule.toDto(): MikrotikRuleDto = MikrotikRuleDto(
    uid = this.uid,
    type = this.type.name,
    ruleNumber = this.ruleNumber,
    description = this.description,
    enabled = this.enabled,
    autoOff = this.autoOff,
    autoOn = this.autoOn
)
