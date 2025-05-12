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
    
    // Список правил с * - инициализируется после загрузки конфигурации
    var formattedRulesList: List<String> = emptyList()
    
    @PostConstruct
    fun init() {
        // Добавляем * к ID правил, если его нет
        formattedRulesList = rulesList.map { 
            if (it.startsWith("*")) it else "*$it" 
        }
    }
}