package com.baybaka.mikrotikcontrol.infrastructure.config

import com.baybaka.mikrotikcontrol.application.service.MikrotikConfigProperties
import com.baybaka.mikrotikcontrol.domain.model.MikrotikRule
import com.baybaka.mikrotikcontrol.domain.repository.RuleRepository
import com.baybaka.mikrotikcontrol.infrastructure.feign.FirewallRuleResponse
import com.baybaka.mikrotikcontrol.infrastructure.feign.MikrotikApiClient
import com.baybaka.mikrotikcontrol.infrastructure.feign.QueueResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.boot.ApplicationArguments
import java.util.*

class RuleInitializerTest {

    private lateinit var mockRepository: RuleRepository
    private lateinit var mockApiClient: MikrotikApiClient
    private lateinit var mockConfigProperties: MikrotikConfigProperties
    private lateinit var mockArgs: ApplicationArguments
    private lateinit var ruleInitializer: RuleInitializer
    
    // Capture saved rules for assertions
    private val savedRules = mutableListOf<MikrotikRule>()

    @BeforeEach
    fun setUp() {
        savedRules.clear()
        
        mockRepository = mock(RuleRepository::class.java)
        mockApiClient = mock(MikrotikApiClient::class.java)
        mockConfigProperties = mock(MikrotikConfigProperties::class.java)
        mockArgs = mock(ApplicationArguments::class.java)

        // Configure behavior for repository.saveAll to capture rules
        `when`(mockRepository.saveAll(anyList<MikrotikRule>())).thenAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val rules = invocation.getArgument(0) as List<MikrotikRule>
            savedRules.addAll(rules)
            rules // return the original list
        }

        // Configure mocks
        `when`(mockConfigProperties.formattedRulesList).thenReturn(listOf("*1", "*2"))
        `when`(mockConfigProperties.formattedQueueList).thenReturn(listOf("*3", "*4"))

        ruleInitializer = RuleInitializer(mockConfigProperties, mockRepository, mockApiClient)
    }

    @Test
    fun `should load firewall rules from API and save them to repository`() {
        // Arrange
        val firewallRules = listOf(
            FirewallRuleResponse("*1", false, "Test rule 1", null, null),
            FirewallRuleResponse("*2", true, "Test rule 2", null, null),
            FirewallRuleResponse("*99", false, "Some other rule", null, null)
        )
        `when`(mockApiClient.getFirewallRuleAll()).thenReturn(firewallRules)
        `when`(mockApiClient.getSimpleQueueRules()).thenReturn(emptyList())

        // Act
        ruleInitializer.run(mockArgs)

        // Assert
        assertEquals(2, savedRules.size)
        assertTrue(savedRules.all { it.type == MikrotikRule.RuleType.FIREWALL })
        
        val rule1 = savedRules.find { it.uid == "*1" }
        assertEquals("Test rule 1", rule1?.description)
        assertEquals(true, rule1?.enabled)
        
        val rule2 = savedRules.find { it.uid == "*2" }
        assertEquals("Test rule 2", rule2?.description)
        assertEquals(false, rule2?.enabled)
    }

    @Test
    fun `should load queue rules from API and save them to repository`() {
        // Arrange
        val queueRules = listOf(
            QueueResponse("*3", false, "Queue 1", null, null),
            QueueResponse("*4", true, "Queue 2", null, null),
            QueueResponse("*99", false, "Some other queue", null, null)
        )
        `when`(mockApiClient.getFirewallRuleAll()).thenReturn(emptyList())
        `when`(mockApiClient.getSimpleQueueRules()).thenReturn(queueRules)

        // Act
        ruleInitializer.run(mockArgs)

        // Assert
        assertEquals(2, savedRules.size)
        assertTrue(savedRules.all { it.type == MikrotikRule.RuleType.QUEUE })
        
        val rule1 = savedRules.find { it.uid == "*3" }
        assertEquals("Queue 1", rule1?.description)
        assertEquals(true, rule1?.enabled)
        
        val rule2 = savedRules.find { it.uid == "*4" }
        assertEquals("Queue 2", rule2?.description)
        assertEquals(false, rule2?.enabled)
    }

    @Test
    fun `should load both firewall and queue rules`() {
        // Arrange
        val firewallRules = listOf(
            FirewallRuleResponse("*1", false, "Test rule 1", null, null)
        )
        val queueRules = listOf(
            QueueResponse("*3", false, "Queue 1", null, null)
        )
        `when`(mockApiClient.getFirewallRuleAll()).thenReturn(firewallRules)
        `when`(mockApiClient.getSimpleQueueRules()).thenReturn(queueRules)

        // Act
        ruleInitializer.run(mockArgs)

        // Assert
        assertEquals(2, savedRules.size)
        assertTrue(savedRules.any { it.type == MikrotikRule.RuleType.FIREWALL })
        assertTrue(savedRules.any { it.type == MikrotikRule.RuleType.QUEUE })
    }

    @Test
    fun `should handle exception when API call fails`() {
        // Create a fresh mock to avoid behavior from the shared mock
        val testRepository = mock(RuleRepository::class.java)
        val testInitializer = RuleInitializer(mockConfigProperties, testRepository, mockApiClient)
        
        // Arrange
        `when`(mockApiClient.getFirewallRuleAll()).thenThrow(RuntimeException("API Error"))
        `when`(mockApiClient.getSimpleQueueRules()).thenReturn(emptyList())

        // Act
        testInitializer.run(mockArgs)

        // Assert
        verify(testRepository, never()).saveAll(anyList<MikrotikRule>())
    }

    @Test
    fun `should handle empty rule lists in configuration`() {
        // Arrange
        `when`(mockConfigProperties.formattedRulesList).thenReturn(emptyList())
        `when`(mockConfigProperties.formattedQueueList).thenReturn(emptyList())

        // Act
        ruleInitializer.run(mockArgs)

        // Assert
        assertTrue(savedRules.isEmpty())
    }
    
    @Test
    fun `should correctly parse comment fields`() {
        // Arrange
        val firewallRules = listOf(
            FirewallRuleResponse("*1", false, "auto_off #description Rule with auto off", null, null),
            FirewallRuleResponse("*2", true, "auto_on #description Rule with auto on", null, null)
        )
        `when`(mockApiClient.getFirewallRuleAll()).thenReturn(firewallRules)
        `when`(mockApiClient.getSimpleQueueRules()).thenReturn(emptyList())

        // Act
        ruleInitializer.run(mockArgs)

        // Assert
        assertEquals(2, savedRules.size)
        
        val rule1 = savedRules.find { it.uid == "*1" }
        assertEquals("Rule with auto off", rule1?.description)
        assertEquals(true, rule1?.autoOff)
        assertEquals(false, rule1?.autoOn)
        
        val rule2 = savedRules.find { it.uid == "*2" }
        assertEquals("Rule with auto on", rule2?.description)
        assertEquals(true, rule2?.autoOn)
        assertEquals(false, rule2?.autoOff)
    }
    
    // Helper method for generic list type
    private inline fun <reified T> anyList(): List<T> {
        return Mockito.anyList<T>()
    }
}
