package com.baybaka.mikrotikcontrol.domain.service

import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule

/**
 * Порт для взаимодействия с API Mikrotik (интерфейс адаптера)
 */
interface MikrotikApiPort {
    /**
     * Получить текущее состояние правила с сервера Mikrotik
     */
    fun getRuleState(rule: MikrotikRule): Boolean
    
    /**
     * Изменить состояние правила на сервере Mikrotik
     */
    fun toggleRule(rule: MikrotikRule, enable: Boolean): Boolean

}
