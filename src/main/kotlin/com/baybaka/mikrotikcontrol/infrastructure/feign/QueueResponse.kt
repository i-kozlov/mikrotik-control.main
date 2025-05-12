package com.baybaka.mikrotikcontrol.infrastructure.feign

import com.fasterxml.jackson.annotation.JsonProperty

data class QueueResponse(
    @JsonProperty(".id") val id: String,
    var disabled: Boolean = false,
    val comment: String? = null,
    val time: String? = null,
    @JsonProperty(".about")
    val about: String? = null
)
