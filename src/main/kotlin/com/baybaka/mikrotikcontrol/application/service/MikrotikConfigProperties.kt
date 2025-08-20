package com.baybaka.mikrotikcontrol.application.service

import com.baybaka.mikrotikcontrol.domain.model.GroupConfig
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация для отдельного правила
 */
data class RuleConfig(
    val id: String,
    val description: String? = null,
    val group: String = "default",
    val hideToggle: Boolean = false
)

/**
 * Параметры конфигурации для работы с MikroTik
 */
@Configuration
@ConfigurationProperties(prefix = "mikrotik")
class MikrotikConfigProperties {
    lateinit var username: String
    lateinit var password: String
    
    // Старый формат для обратной совместимости
    var rulesList: List<String> = emptyList()
    var simpleQueueList: List<String> = emptyList()

    // Группы с настройками порядка и развертывания
    var groups: Map<String, GroupConfig> = emptyMap()

    // Новый формат с поддержкой описания и групп
    var firewallRules: List<RuleConfig> = emptyList()
    var queueRules: List<RuleConfig> = emptyList()

    // Список правил с * - инициализируется после загрузки конфигурации
    var formattedRulesList: List<String> = emptyList()
    var formattedQueueList: List<String> = emptyList()

    // Карты для быстрого поиска конфигурации по ID
    var firewallRuleConfigMap: Map<String, RuleConfig> = emptyMap()
    var queueRuleConfigMap: Map<String, RuleConfig> = emptyMap()

    @PostConstruct
    fun init() {
        // Обрабатываем старый формат
        formattedRulesList = rulesList.map { it.ensurePrefix() }
        formattedQueueList = simpleQueueList.map { it.ensurePrefix() }
        
        // Обрабатываем новый формат
        firewallRuleConfigMap = firewallRules.associate { it.id.ensurePrefix() to it }
        queueRuleConfigMap = queueRules.associate { it.id.ensurePrefix() to it }
        
        // Объединяем старый и новый форматы
        val additionalFirewallIds = firewallRules.map { it.id.ensurePrefix() }
        val additionalQueueIds = queueRules.map { it.id.ensurePrefix() }
        
        formattedRulesList = (formattedRulesList + additionalFirewallIds).distinct()
        formattedQueueList = (formattedQueueList + additionalQueueIds).distinct()
        
        // Добавляем default группу если её нет в конфигурации
        if (!groups.containsKey("default")) {
            groups = groups + ("default" to GroupConfig(
                name = "Прочие правила",
                order = 999,
                expanded = false
            ))
        }
    }

    private fun String.ensurePrefix() = if (startsWith("*")) this else "*$this"
    
    /**
     * Получает конфигурацию правила фаервола по ID
     */
    fun getFirewallRuleConfig(id: String): RuleConfig? {
        return firewallRuleConfigMap[id]
    }
    
    /**
     * Получает конфигурацию правила очереди по ID
     */
    fun getQueueRuleConfig(id: String): RuleConfig? {
        return queueRuleConfigMap[id]
    }
    
    /**
     * Получает все сконфигурированные ID правил фаервола в порядке конфигурации
     */
    fun getOrderedFirewallIds(): List<String> {
        return rulesList.map { it.ensurePrefix() } + firewallRules.map { it.id.ensurePrefix() }
    }
    
    /**
     * Получает все сконфигурированные ID правил очередей в порядке конфигурации
     */
    fun getOrderedQueueIds(): List<String> {
        return simpleQueueList.map { it.ensurePrefix() } + queueRules.map { it.id.ensurePrefix() }
    }
    
    /**
     * Получает конфигурацию группы по имени
     */
    fun getGroupConfig(groupName: String): GroupConfig {
        return groups[groupName] ?: GroupConfig(
            name = groupName,
            order = 999,
            expanded = false
        )
    }
    
    /**
     * Получает все группы, отсортированные по order + name
     */
    fun getSortedGroups(): List<Pair<String, GroupConfig>> {
        return groups.toList().sortedWith(
            compareBy<Pair<String, GroupConfig>> { it.second.order }
                .thenBy { it.first }
        )
    }
}