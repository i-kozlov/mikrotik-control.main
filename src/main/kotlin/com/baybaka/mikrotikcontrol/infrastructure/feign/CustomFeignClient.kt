package com.baybaka.mikrotikcontrol.infrastructure.feign

import feign.Client
import feign.Request
import feign.Response
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Кастомный Feign клиент, который заменяет %2A на * в URL непосредственно перед отправкой запроса
 */
@Component
@Primary
class CustomFeignClient(private val delegate: Client) : Client {
    
    private val logger = LoggerFactory.getLogger(CustomFeignClient::class.java)
    
    override fun execute(request: Request, options: Request.Options): Response {
        // Получаем URL запроса
        val url = request.url()
        
        // Если URL содержит %2A (закодированная *), заменяем на *
        if (url.contains("%2A")) {
            val fixedUrl = url.replace("%2A", "*")
            logger.info("URL changed from: {} to: {}", url, fixedUrl)
            
            // Создаем новый запрос с исправленным URL
            val newRequest = Request.create(
                request.httpMethod(),
                fixedUrl,
                request.headers(),
                request.body(),
                request.charset(),
                request.requestTemplate()
            )
            
            // Выполняем запрос с исправленным URL
            return delegate.execute(newRequest, options)
        }
        
        // Если URL не содержит %2A, используем оригинальный запрос
        return delegate.execute(request, options)
    }
}
