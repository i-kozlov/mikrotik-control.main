package com.baybaka.mikrotikcontrol.presentation.dto

import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.model.ScheduledTask

/**
 * DTO для передачи информации о правиле MikroTik
 */
data class MikrotikRuleDto(
    val uid: String,
    val type: String,
    val ruleNumber: String,
    val description: String,
    val group: String,
    val enabled: Boolean,
    val autoOff: Boolean,
    val autoOn: Boolean,
    val scheduled: Boolean,
    val inactiveTime: Boolean,
    val hideToggle: Boolean
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
fun MikrotikRule.toDto(): MikrotikRuleDto {
    return MikrotikRuleDto(
        uid = this.uid,
        type = this.type.name,
        ruleNumber = this.ruleNumber,
        description = this.description,
        group = this.group,
        enabled = this.enabled,
        autoOff = this.autoOff,
        autoOn = this.autoOn,
        scheduled = this.scheduled,
        inactiveTime = this.inactiveTime,
        hideToggle = this.hideToggle
    )
}