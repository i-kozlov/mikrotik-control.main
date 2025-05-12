package com.baybaka.mikrotikcontrol.domain.service

/**
 * Сервис для расчета степени похожести двух строк
 */
interface StringSimilarityService {
    /**
     * Рассчитывает степень сходства двух строк (от 0.0 до 1.0)
     * @param s1 Первая строка
     * @param s2 Вторая строка
     * @return Коэффициент сходства (1.0 = идентичные строки)
     */
    fun calculateSimilarity(s1: String, s2: String): Double
    
    /**
     * Проверяет, достаточно ли похожи строки
     * @param s1 Первая строка
     * @param s2 Вторая строка
     * @param threshold Минимальный порог сходства
     * @return true если степень сходства >= threshold
     */
    fun isSimilarEnough(s1: String, s2: String, threshold: Double): Boolean {
        return calculateSimilarity(s1, s2) >= threshold
    }
}
