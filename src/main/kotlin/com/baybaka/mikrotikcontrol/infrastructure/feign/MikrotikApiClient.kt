package com.baybaka.mikrotikcontrol.infrastructure.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

/**
 * Feign-клиент для взаимодействия с REST API MikroTik
 */
@FeignClient(
    name = "mikrotik-api",
    url = "\${mikrotik.router-url}/rest",
    configuration = [MikrotikFeignConfig::class]
)
interface MikrotikApiClient {
    
    /**
     * Получить все правила фаервола
     */
    @GetMapping("ip/firewall/filter")
    fun getFirewallRuleAll(): List<FirewallRuleResponse>
    
    /**
     * Получить правило фаервола по номеру
     */
    @GetMapping("ip/firewall/filter/{ruleNumber}")
    fun getFirewallRule(@PathVariable ruleNumber: String): FirewallRuleResponse
    
    /**
     * Получить правило очереди по номеру
     */
    @GetMapping("/queue/simple/{ruleNumber}")
    fun getQueueRule(@PathVariable ruleNumber: String): QueueResponse
    
    /**
     * Обновить правило фаервола
     */
    @PutMapping("/ip/firewall/filter/{ruleNumber}")
    fun updateFirewallRule(
        @PathVariable ruleNumber: String, 
        @RequestBody updates: Map<String, Any>
    ): Map<String, Any>
    
    /**
     * Обновить правило очереди
     */
    @PutMapping("/queue/simple/{ruleNumber}")
    fun updateQueueRule(
        @PathVariable ruleNumber: String, 
        @RequestBody updates: Map<String, Any>
    ): Map<String, Any>
}