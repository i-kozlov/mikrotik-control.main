package com.baybaka.mikrotikcontrol.infrastructure.repository

import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.repository.RuleRepository
import org.springframework.stereotype.Repository


/**
 * In-memory реализация репозитория правил
 * Использует LinkedHashMap для сохранения порядка правил из конфигурации
 */
@Repository
class InMemoryRuleRepository : RuleRepository {
    
    private val rules = LinkedHashMap<String, MikrotikRule>()
    
    override fun findByUid(uid: String): MikrotikRule? {
        return rules[uid]
    }
    
    override fun findAll(): List<MikrotikRule> {
        return rules.values.toList()
    }
    
    override fun save(rule: MikrotikRule) {
        rules[rule.uid] = rule
    }
    
    override fun saveAll(rules: List<MikrotikRule>) {
        rules.forEach { save(it) }
    }
}
