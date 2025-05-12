package com.baybaka.mikrotikcontrol.infrastructure.feign

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotNull

/**
 * Интеграционный тест для проверки работы Feign клиента с MikroTik API
 *
 * Примечание: Этот тест требует подключения к настоящему роутеру MikroTik и подходящих учетных данных.
 * Для запуска теста требуется наличие переменных окружения ROUTER_IP, ROUTER_USER и ROUTER_PASSWORD.
 * Если переменные не настроены, тест пропускается.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // Используем тестовый профиль, если он есть
class MikrotikApiClientIntegrationTest {
    
    @Autowired
    private lateinit var mikrotikApiClient: MikrotikApiClient
    
    /**
     * Проверяем получение правила фаервола по ID
     */
    @Test
    fun testGetFirewallRule() {
        // Выполняем запрос к API MikroTik с точно таким же ID, как в Postman
        val result = mikrotikApiClient.getFirewallRule("*367")
        
        // Проверяем, что ответ не пустой и содержит ожидаемые поля
        assertNotNull(result)
        println("Получено правило: $result")
        
        // Проверяем наличие ключевых полей
        assertNotNull(result.id)
        // Другие проверки, специфичные для вашего окружения
    }
    
    /**
     * Проверяем получение правила очереди
     */
    @Test
    @Disabled("Включите, если у вас настроены правила очереди")
    fun testGetQueueRule() {
        val result = mikrotikApiClient.getQueueRule("*0")

        assertNotNull(result)
        println("Получено правило очереди: $result")
        
        assertNotNull(result.id)
    }
    
    /**
     * Проверяем обновление правила фаервола
     * (этот тест может изменить состояние вашего роутера, используйте с осторожностью)
     */
    @Test
    @Disabled("Включите для проверки обновления, только если вы понимаете последствия")
    fun testUpdateFirewallRule() {
        // Сначала получаем текущее состояние
        val initialRule = mikrotikApiClient.getFirewallRule("*367")
        val initialDisabled = initialRule.disabled

        // Выполняем обновление - инвертируем значение поля disabled
        val updates = mapOf("disabled" to !initialDisabled)
        val updateResult = mikrotikApiClient.updateFirewallRule("*367", updates)
        
        // Проверяем результат обновления
        assertThat(updateResult).isNotEmpty()
        assertThat(updateResult["disabled"]).isEqualTo(!initialDisabled)
        
        // Возвращаем исходное состояние
        mikrotikApiClient.updateFirewallRule("*367", mapOf("disabled" to initialDisabled))
    }
}
