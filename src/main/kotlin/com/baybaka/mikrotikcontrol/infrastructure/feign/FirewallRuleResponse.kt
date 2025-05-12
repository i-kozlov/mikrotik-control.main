package com.baybaka.mikrotikcontrol.infrastructure.feign

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Класс для десериализации ответа от API Mikrotik о правиле фаервола
 */
data class FirewallRuleResponse(
    @JsonProperty(".id") val id: String,
    var disabled: Boolean = false,
    val comment: String? = null,
    val time: String? = null,
    @JsonProperty(".about")
    val about: String? = null,
    val action: String? = null,
    val bytes: String? = null,
    val chain: String? = null,
    val dynamic: Boolean? = null,
    val invalid: String? = null,
    val log: Boolean? = null,
    val packets: String? = null,
    val protocol: String? = null,

    // Дополнительные поля для конкретных правил
    @JsonProperty("dst-port") val dstPort: String? = null,
    @JsonProperty("in-interface-list") val inInterfaceList: String? = null,
    @JsonProperty("src-address-list") val srcAddressList: String? = null,
    @JsonProperty("log-prefix") val logPrefix: String? = null
)