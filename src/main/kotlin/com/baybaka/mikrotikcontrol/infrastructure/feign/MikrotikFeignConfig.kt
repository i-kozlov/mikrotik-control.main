package com.baybaka.mikrotikcontrol.infrastructure.feign

import com.baybaka.mikrotikcontrol.application.service.MikrotikConfigProperties
import feign.Client
import feign.RequestInterceptor
import feign.auth.BasicAuthRequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary


/**
 * Конфигурация Feign клиента для MikroTik API
 */
@Configuration
class MikrotikFeignConfig(private val properties: MikrotikConfigProperties) {
    
    /**
     * Настраивает Basic Auth для запросов к MikroTik API
     */
    @Bean
    fun basicAuthRequestInterceptor(): RequestInterceptor {
        return BasicAuthRequestInterceptor(properties.username, properties.password)
    }
    
    /**
     * Создает экземпляр стандартного Feign клиента
     */
    @Bean
    fun feignClient(): Client {
        return Client.Default(null, null)
    }

    /**
     * Оборачивает стандартный клиент в наш кастомный,
     * который обрабатывает специальные символы в URL
     */
    @Bean
    @Primary
    fun customFeignClient(client: Client): CustomFeignClient {
        return CustomFeignClient(client)
    }
}