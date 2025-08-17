package com.baybaka.mikrotikcontrol.application.service

import com.baybaka.mikrotikcontrol.domain.exception.EntityNotFoundException
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.model.GroupConfig
import com.baybaka.mikrotikcontrol.domain.repository.RuleRepository
import com.baybaka.mikrotikcontrol.domain.service.MikrotikApiPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Сервис управления правилами MikroTik (основной сервис приложения)
 */
@Service
class RuleManagementService(
    private val ruleRepository: RuleRepository,
    private val mikrotikApiPort: MikrotikApiPort,
    private val configProperties: MikrotikConfigProperties
) {
    private val logger = LoggerFactory.getLogger(RuleManagementService::class.java)
    
    /**
     * Получить текущее состояние правила
     */
    fun getRuleState(uid: String): MikrotikRule {
        val rule = ruleRepository.findByUid(uid) 
            ?: throw EntityNotFoundException("Правило с UID $uid не найдено")
        
        // Получаем актуальное состояние с устройства
        val enabled = mikrotikApiPort.getRuleState(rule)
        rule.enabled = enabled
        
        return rule
    }
    
    /**
     * Переключить состояние правила
     */
    fun toggleRule(uid: String, enable: Boolean): MikrotikRule {
        val rule = getRuleState(uid)
        
        if (rule.enabled == enable) {
            logger.info("Правило ${rule.uid} уже в состоянии enabled=$enable, изменения не требуются")
            return rule
        }
        
        // Меняем состояние на устройстве
        val success = mikrotikApiPort.toggleRule(rule, enable)
        
        if (success) {
            rule.enabled = enable
            ruleRepository.save(rule)
            logger.info("Правило ${rule.uid} успешно изменено на enabled=$enable")
        } else {
            logger.error("Не удалось изменить состояние правила ${rule.uid}")
        }
        
        return rule
    }
    
    /**
     * Массовое переключение состояния правил
     */
    fun toggleRules(uids: List<String>, enable: Boolean): List<MikrotikRule> {
        return uids.map { toggleRule(it, enable) }
    }
    
    /**
     * Получить все правила
     */
    fun getAllRules(): List<MikrotikRule> {
        return ruleRepository.findAll()
    }
    
    /**
     * Получить отсортированные конфигурации групп
     */
    fun getSortedGroupConfigs(): List<Pair<String, GroupConfig>> {
        return configProperties.getSortedGroups()
    }
}
