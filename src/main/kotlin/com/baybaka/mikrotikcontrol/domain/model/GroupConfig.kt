package com.baybaka.mikrotikcontrol.domain.model

/**
 * Конфигурация группы правил
 */
data class GroupConfig(
    val name: String,
    val order: Int = 999,
    val expanded: Boolean = false
)