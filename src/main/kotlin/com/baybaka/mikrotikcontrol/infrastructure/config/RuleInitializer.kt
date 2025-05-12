package com.baybaka.mikrotikcontrol.infrastructure.config

import com.baybaka.mikrotikcontrol.application.service.MikrotikConfigProperties
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule.RuleType
import com.baybaka.mikrotikcontrol.domain.repository.RuleRepository
import com.baybaka.mikrotikcontrol.infrastructure.feign.FirewallRuleResponse
import com.baybaka.mikrotikcontrol.infrastructure.feign.MikrotikApiClient
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
            val allRules = apiClient.getFirewallRuleAll()
            
            // Получаем список ID с добавлением "*"
            val configuredIds = configProperties.formattedRulesList
            
            logger.info("Настроенные ID правил: $configuredIds")
            
            // Преобразуем ответ от API в доменные объекты
            val rules = mutableListOf<MikrotikRule>()
            
            // Обрабатываем каждое правило из списка
            for (rule in allRules) {
                val id = rule.id
                if (configuredIds.contains(id)) {
                    val mikrotikRule = createRuleFromApiData(rule)
                    rules.add(mikrotikRule)
                }
            }
            
            // Сохраняем правила
            ruleRepository.saveAll(rules)
            
            logger.info("Загружено ${rules.size} правил из MikroTik API")
        } catch (e: Exception) {
            logger.error("Ошибка при загрузке правил: ${e.message}", e)
        }
    }
    
    /**
     * Создает доменный объект правила из данных API
     */
    private fun createRuleFromApiData(rule: FirewallRuleResponse): MikrotikRule {
        val id = rule.id
        val comment = rule.comment ?: ""
        val isDisabled = rule.disabled
        
        // Извлекаем описание
        val description = if (comment.contains("#description")) {
            comment.substringAfter("#description", comment)
        } else {
            comment
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
}