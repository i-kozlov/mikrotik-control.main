package com.baybaka.mikrotikcontrol.infrastructure.adapter

import com.baybaka.mikrotikcontrol.domain.exception.MikrotikApiException
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule.RuleType
import com.baybaka.mikrotikcontrol.domain.service.MikrotikApiPort
import com.baybaka.mikrotikcontrol.infrastructure.feign.MikrotikApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import feign.FeignException
import lombok.extern.slf4j.Slf4j

/**
 * Адаптер для MikroTik API, реализующий порт домена
 */
@Component
class MikrotikApiAdapter(private val apiClient: MikrotikApiClient) : MikrotikApiPort {
    
    private val logger = LoggerFactory.getLogger(MikrotikApiAdapter::class.java)
    
    override fun getRuleState(rule: MikrotikRule): Boolean {
        try {
            val response = isDisable(rule)
            return !response
        } catch (e: Exception) {
            logger.error("Ошибка при получении состояния правила ${rule.uid}", e)
            throw MikrotikApiException("Не удалось получить состояние правила", e)
        }
    }
    
    override fun toggleRule(rule: MikrotikRule, enable: Boolean): Boolean {
        try {
            val updates = mapOf("disabled" to !enable)
            logger.info("Изменение состояния правила ${rule.uid} ${rule.type} . body {$updates}")
            
            when (rule.type) {
                RuleType.FIREWALL -> apiClient.updateFirewallRule(rule.ruleNumber, updates)
                RuleType.QUEUE -> apiClient.updateQueueRule(rule.ruleNumber, updates)
            }
            
            return true
        } catch (e: Exception) {
            logger.error("Ошибка при изменении состояния правила ${rule.uid}", e)
            return false
        }
    }

    private fun isDisable(rule: MikrotikRule): Boolean {
        return try {
            when (rule.type) {
                RuleType.FIREWALL -> apiClient.getFirewallRule(rule.ruleNumber).disabled
                RuleType.QUEUE -> false
            }
        } catch (e: FeignException) {
            logger.error("Ошибка при запросе к MikroTik API: ${e.message}")
            throw MikrotikApiException("Ошибка при запросе к MikroTik API", e)
        }
    }

}
