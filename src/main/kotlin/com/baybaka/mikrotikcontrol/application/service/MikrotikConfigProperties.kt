package com.baybaka.mikrotikcontrol.application.service

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Параметры конфигурации для работы с MikroTik
 */
@Configuration
@ConfigurationProperties(prefix = "mikrotik")
class MikrotikConfigProperties {
    lateinit var username: String
    lateinit var password: String
    
    // Список из конфигурации
    var rulesList: List<String> = emptyList()
    var simpleQueueList: List<String> = emptyList()

    // Список правил с * - инициализируется после загрузки конфигурации
    var formattedRulesList: List<String> = emptyList()
    var formattedQueueList: List<String> = emptyList()

    @PostConstruct
    fun init() {
        formattedRulesList = rulesList.map { it.ensurePrefix() }
        formattedQueueList = simpleQueueList.map { it.ensurePrefix() }
    }

    private fun String.ensurePrefix() = if (startsWith("*")) this else "*$this"
}