package com.baybaka.mikrotikcontrol.domain.model

/**
 * Представление правила маршрутизатора Mikrotik в доменной модели
 */
data class MikrotikRule(
    val uid: String,
    val type: RuleType,
    val ruleNumber: String,
    val description: String,
    val group: String = "default",
    var enabled: Boolean = false,
    val autoOff: Boolean = false,
    val autoOn: Boolean = false,
    val scheduled: Boolean = false,
    val inactiveTime: Boolean = false,
    val hideToggle: Boolean = false
) {
    enum class RuleType {
        FIREWALL, QUEUE
    }
}