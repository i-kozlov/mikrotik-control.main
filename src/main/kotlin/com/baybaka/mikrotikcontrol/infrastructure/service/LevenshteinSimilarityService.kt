package com.baybaka.mikrotikcontrol.infrastructure.service

import com.baybaka.mikrotikcontrol.domain.service.StringSimilarityService
import org.springframework.stereotype.Service
import kotlin.math.max

/**
 * Реализация сервиса сравнения строк на основе расстояния Левенштейна
 */
@Service
class LevenshteinSimilarityService : StringSimilarityService {
    
    override fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        
        val maxLength = max(s1.length, s2.length)
        if (maxLength == 0) return 1.0
        
        val distance = calculateLevenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLength)
    }
    
    /**
     * Рассчитывает расстояние Левенштейна между двумя строками
     */
    private fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        
        // Если одна из строк пуста, расстояние равно длине другой строки
        if (m == 0) return n
        if (n == 0) return m
        
        // Матрица для динамического программирования
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        // Инициализация первой строки и столбца
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        // Заполнение матрицы
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // удаление
                    dp[i][j - 1] + 1,      // вставка
                    dp[i - 1][j - 1] + cost // замена или совпадение
                )
            }
        }
        
        return dp[m][n]
    }
}
