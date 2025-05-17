package com.baybaka.mikrotikcontrol.application.service

import com.baybaka.mikrotikcontrol.domain.model.ScheduledTask
import com.baybaka.mikrotikcontrol.domain.repository.ScheduledTaskRepository
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Сервис управления запланированными задачами
 */
@Service
class SchedulerService(
    private val taskRepository: ScheduledTaskRepository,
    private val ruleManagementService: RuleManagementService
) {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    /**
     * Запланировать временное изменение состояния правила
     */
    fun scheduleRuleStateChange(ruleUid: String, targetState: Boolean, minutes: Int): ScheduledTask {
        // Отменяем существующие задачи для этого правила
        cancelTasksForRule(ruleUid)
        
        // Создаем новую задачу
        val executionTime = Instant.now().plus(minutes.toLong(), ChronoUnit.MINUTES)
        val task = ScheduledTask(
            ruleUid = ruleUid,
            targetState = targetState,
            executionTime = executionTime
        )
        
        // Сохраняем задачу
        val savedTask = taskRepository.save(task)
        
        // Запланировать выполнение
        scheduleExecution(savedTask)
        
        return savedTask
    }

    /**
     * Запланировать выполнение задачи
     */
    private fun scheduleExecution(task: ScheduledTask) {
        val now = Instant.now()
        val delay = java.time.Duration.between(now, task.executionTime)
        
        if (delay.isNegative || delay.isZero) {
            // Если задача уже должна быть выполнена, выполняем её сразу
            executeTask(task)
        } else {
            // Иначе запланировать выполнение
            scheduler.schedule(
                { executeTask(task) },
                delay.toMillis(),
                TimeUnit.MILLISECONDS
            )
            logger.info("Задача запланирована: правило ${task.ruleUid} будет переключено в состояние ${task.targetState} в ${task.executionTime}")
        }
    }

    /**
     * Выполнить задачу
     */
    private fun executeTask(task: ScheduledTask) {
        try {
            // Изменить состояние правила
            ruleManagementService.toggleRule(task.ruleUid, task.targetState)
            
            // Пометить задачу как выполненную
            taskRepository.markAsCompleted(task.id)
            
            logger.info("Задача выполнена: правило ${task.ruleUid} переключено в состояние ${task.targetState}")
        } catch (e: Exception) {
            logger.error("Ошибка при выполнении задачи ${task.id}: ${e.message}", e)
        }
    }

    /**
     * Отменить задачи для правила
     */
    fun cancelTasksForRule(ruleUid: String) {
        taskRepository.deleteByRuleUid(ruleUid)
        logger.info("Отменены все задачи для правила $ruleUid")
    }

    /**
     * Получить все активные задачи
     */
    fun getAllActiveTasks(): List<ScheduledTask> {
        return taskRepository.findAll().filter { !it.completed }
    }
    
    /**
     * Проверить, есть ли активная запланированная задача для правила
     */
    fun getActiveTaskForRule(ruleUid: String): ScheduledTask? {
        return taskRepository.findAll()
            .filter { !it.completed && it.ruleUid == ruleUid }
            .minByOrNull { it.executionTime }
    }
    
    /**
     * Получить оставшееся время в минутах до выполнения задачи
     */
    fun getRemainingMinutes(task: ScheduledTask): Long {
        val now = Instant.now()
        return if (task.executionTime.isAfter(now)) {
            Duration.between(now, task.executionTime).toMinutes()
        } else {
            0L
        }
    }

    /**
     * Проверить просроченные задачи и выполнить их
     */
    fun executeOverdueTasks() {
        val now = Instant.now()
        val overdueTasks = taskRepository.findActiveTasksDueBefore(now)
        
        overdueTasks.forEach { executeTask(it) }
        
        if (overdueTasks.isNotEmpty()) {
            logger.info("Выполнено ${overdueTasks.size} просроченных задач")
        }
    }

    /**
     * Инициализация после создания бина
     */
    @PostConstruct
    fun init() {
        // Проверить, есть ли задачи, которые нужно выполнить
        executeOverdueTasks()
        
        // Запланировать выполнение активных задач
        val activeTasks = taskRepository.findAll().filter { !it.completed }
        activeTasks.forEach { scheduleExecution(it) }
        
        logger.info("Планировщик запущен, запланировано ${activeTasks.size} задач")
    }

    /**
     * Освобождение ресурсов перед уничтожением бина
     */
    @PreDestroy
    fun shutdown() {
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
        }
        logger.info("Планировщик остановлен")
    }
}
