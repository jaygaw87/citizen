package com.example.data.learning

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Calculates genuine study streaks based on recorded daily activity dates.
 * A study day requires genuine user activity (studying a question, flashcards, practice, or speaking).
 */
object StudyStreakCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Calculates the current consecutive streak in days.
     * If active today: streak includes today and all uninterrupted prior days.
     * If active yesterday but not yet today: streak is alive including yesterday and prior days.
     * If neither today nor yesterday was active: streak is 0.
     */
    fun calculateStreak(activeDates: Set<String>, currentDate: Date = Date()): Int {
        if (activeDates.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        // Reset time components
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val todayStr = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(calendar.time)

        val hasToday = activeDates.contains(todayStr)
        val hasYesterday = activeDates.contains(yesterdayStr)

        if (!hasToday && !hasYesterday) {
            return 0
        }

        var streak = 0

        // Reset calendar
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (hasToday) {
            streak++
            // Check day before today, then day before that, etc.
            while (true) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val checkDate = dateFormat.format(calendar.time)
                if (activeDates.contains(checkDate)) {
                    streak++
                } else {
                    break
                }
            }
        } else {
            // Has yesterday
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1) // at yesterday now
            while (true) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val checkDate = dateFormat.format(calendar.time)
                if (activeDates.contains(checkDate)) {
                    streak++
                } else {
                    break
                }
            }
        }

        return streak
    }

    fun getTodayDateString(currentDate: Date = Date()): String {
        return dateFormat.format(currentDate)
    }
}
