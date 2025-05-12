package com.baybaka.mikrotikcontrol.domain.repository

import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule

/**
 * Интерфейс репозитория для доступа к правилам Mikrotik
 */
interface RuleRepository {
    fun findByUid(uid: String): MikrotikRule?
    fun findAll(): List<MikrotikRule>
    fun save(rule: MikrotikRule)
    fun saveAll(rules: List<MikrotikRule>)
}
