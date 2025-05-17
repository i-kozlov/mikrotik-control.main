package com.baybaka.mikrotikcontrol.infrastructure.repository

import com.baybaka.mikrotikcontrol.domain.model.ScheduledTask
import com.baybaka.mikrotikcontrol.domain.repository.ScheduledTaskRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory реализация репозитория запланированных задач
 */
@Repository
class InMemoryScheduledTaskRepository : ScheduledTaskRepository {
    private val tasks = ConcurrentHashMap<String, ScheduledTask>()
    
    override fun save(task: ScheduledTask): ScheduledTask {
        tasks[task.id] = task
        return task
    }
    
    override fun findById(id: String): ScheduledTask? {
        return tasks[id]
    }
    
    override fun findAll(): List<ScheduledTask> {
        return tasks.values.toList()
    }
    
    override fun findByRuleUid(ruleUid: String): List<ScheduledTask> {
        return tasks.values.filter { it.ruleUid == ruleUid }
    }
    
    override fun findActiveTasksDueBefore(time: Instant): List<ScheduledTask> {
        return tasks.values.filter { 
            !it.completed && it.executionTime.isBefore(time) 
        }
    }
    
    override fun deleteById(id: String) {
        tasks.remove(id)
    }
    
    override fun deleteByRuleUid(ruleUid: String) {
        val tasksToRemove = tasks.values.filter { it.ruleUid == ruleUid }
        tasksToRemove.forEach { tasks.remove(it.id) }
    }
    
    override fun markAsCompleted(id: String) {
        tasks[id]?.let {
            tasks[id] = it.copy(completed = true)
        }
    }
}
