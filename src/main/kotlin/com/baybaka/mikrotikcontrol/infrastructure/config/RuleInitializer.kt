package com.baybaka.mikrotikcontrol.infrastructure.config

import com.baybaka.mikrotikcontrol.application.service.MikrotikConfigProperties
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule.RuleType
import com.baybaka.mikrotikcontrol.domain.repository.RuleRepository
import com.baybaka.mikrotikcontrol.infrastructure.feign.FirewallRuleResponse
import com.baybaka.mikrotikcontrol.infrastructure.feign.MikrotikApiClient
import com.baybaka.mikrotikcontrol.infrastructure.feign.QueueResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Инициализатор для загрузки правил из MikroTik API при старте приложения
 */
@Component
class RuleInitializer(
    private val configProperties: MikrotikConfigProperties,
    private val ruleRepository: RuleRepository,
    private val apiClient: MikrotikApiClient
) : ApplicationRunner {
    
    private val logger = LoggerFactory.getLogger(RuleInitializer::class.java)
    
    override fun run(args: ApplicationArguments) {
        logger.info("Загрузка правил из MikroTik API...")
        
        try {
            // Получаем все правила из Mikrotik
            val firewallRules = firewallRules()
            val queueRules = queueRules()
            
            // Сохраняем все правила только если оба вызова API успешны
            if (firewallRules != null && queueRules != null) {
                val allRules = firewallRules + queueRules
                ruleRepository.saveAll(allRules)
                
                logger.info("Загружено ${allRules.size} правил из MikroTik API " +
                        "(${firewallRules.size} правил фаервола, ${queueRules.size} правил очередей)")
            } else {
                logger.warn("Пропуск сохранения правил из-за ошибок при получении данных из API")
            }
        } catch (e: Exception) {
            logger.error("Ошибка при загрузке правил: ${e.message}", e)
        }
    }

    /**
     * Получение правил очередей
     */
    private fun queueRules(): List<MikrotikRule>? {
        // Проверяем, настроены ли правила очередей
        if (configProperties.formattedQueueList.isEmpty()) {
            logger.info("Список правил очередей не настроен, пропускаем загрузку")
            return emptyList()
        }
        
        try {
            val allRules = apiClient.getSimpleQueueRules()
            logger.info("Получено ${allRules.size} правил очередей из API")
            
            // Получаем список ID с добавлением "*"
            val configuredIds = configProperties.formattedQueueList
            logger.info("Настроенные ID правил очередей: $configuredIds")
            
            // Преобразуем ответ от API в доменные объекты
            val rules = mutableListOf<MikrotikRule>()
            
            // Обрабатываем каждое правило из списка
            for (rule in allRules) {
                val id = rule.id
                if (configuredIds.contains(id)) {
                    val mikrotikRule = createRuleFromQueueData(rule)
                    rules.add(mikrotikRule)
                }
            }
            return rules
        } catch (e: Exception) {
            logger.error("Ошибка при загрузке правил очередей: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Получение правил фаервола
     */
    private fun firewallRules(): List<MikrotikRule>? {
        // Проверяем, настроены ли правила фаервола
        if (configProperties.formattedRulesList.isEmpty()) {
            logger.info("Список правил фаервола не настроен, пропускаем загрузку")
            return emptyList()
        }
        
        try {
            val allRules = apiClient.getFirewallRuleAll()
            logger.info("Получено ${allRules.size} правил фаервола из API")
            
            // Получаем список ID с добавлением "*"
            val configuredIds = configProperties.formattedRulesList
            logger.info("Настроенные ID правил фаервола: $configuredIds")
            
            // Преобразуем ответ от API в доменные объекты
            val rules = mutableListOf<MikrotikRule>()
            
            // Обрабатываем каждое правило из списка
            for (rule in allRules) {
                val id = rule.id
                if (configuredIds.contains(id)) {
                    val mikrotikRule = createRuleFromFirewallData(rule)
                    rules.add(mikrotikRule)
                }
            }
            return rules
        } catch (e: Exception) {
            logger.error("Ошибка при загрузке правил фаервола: ${e.message}", e)
            return null
        }
    }

    /**
     * Создает доменный объект правила из данных API фаервола
     */
    private fun createRuleFromFirewallData(rule: FirewallRuleResponse): MikrotikRule {
        val id = rule.id
        val comment = rule.comment ?: ""
        val isDisabled = rule.disabled

        // Извлекаем описание
        val description = if (comment.contains("#description")) {
            comment.substringAfter("#description", comment).trim()
        } else {
            comment.trim()
        }

        // Определяем флаги автоматизации
        val autoOff = comment.startsWith("auto_off", ignoreCase = true)
        val autoOn = comment.startsWith("auto_on", ignoreCase = true)
        val inactiveTime = rule.about?.startsWith("inactive time", ignoreCase = true) ?: false
        val scheduled = rule.time?.isNotBlank() ?: false

        return MikrotikRule(
            uid = id,
            type = RuleType.FIREWALL,
            ruleNumber = id,
            description = description,
            enabled = !isDisabled,
            autoOff = autoOff,
            autoOn = autoOn,
            inactiveTime = inactiveTime,
            scheduled = scheduled
        )
    }
    
    /**
     * Создает доменный объект правила из данных API очередей
     */
    private fun createRuleFromQueueData(rule: QueueResponse): MikrotikRule {
        val id = rule.id
        val comment = rule.comment ?: ""
        val isDisabled = rule.disabled

        // Извлекаем описание
        val description = if (comment.contains("#description")) {
            comment.substringAfter("#description", comment).trim()
        } else {
            comment.trim()
        }

        // Определяем флаги автоматизации
        val autoOff = comment.startsWith("auto_off", ignoreCase = true)
        val autoOn = comment.startsWith("auto_on", ignoreCase = true)
        val inactiveTime = rule.about?.startsWith("inactive time", ignoreCase = true) ?: false
        val scheduled = rule.time?.isNotBlank() ?: false

        return MikrotikRule(
            uid = id,
            type = RuleType.QUEUE,
            ruleNumber = id,
            description = description,
            enabled = !isDisabled,
            autoOff = autoOff,
            autoOn = autoOn,
            inactiveTime = inactiveTime,
            scheduled = scheduled
        )
    }
}