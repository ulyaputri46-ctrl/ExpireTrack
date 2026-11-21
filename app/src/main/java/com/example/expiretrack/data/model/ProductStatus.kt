package com.example.expiretrack.data.model

import com.example.expiretrack.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ProductStatus(
    val label: String,
    val textColorRes: Int,
    val bgColorRes: Int,
    val iconRes: Int
) {
    SAFE("Aman", R.color.status_safe, R.color.status_safe_bg, R.drawable.ic_check_circle),
    WARNING("Segera Kedaluwarsa", R.color.status_warning, R.color.status_warning_bg, R.drawable.ic_warning_circle),
    TODAY("Hari Ini", R.color.status_today, R.color.status_today_bg, R.drawable.ic_clock),
    EXPIRED("Kedaluwarsa", R.color.status_expired, R.color.status_expired_bg, R.drawable.ic_alert_circle);

    companion object {
        fun calculateDaysRemaining(expiryDateStr: String?): Long {
            if (expiryDateStr.isNullOrBlank()) return 0L
            return try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val expiryDate = dateFormat.parse(expiryDateStr) ?: return 0L
                val today = getStartOfDay(Date())
                val target = getStartOfDay(expiryDate)
                val diffInMillis = target.time - today.time
                TimeUnit.MILLISECONDS.toDays(diffInMillis)
            } catch (e: Exception) {
                0L
            }
        }

        fun getStatus(expiryDateStr: String?): ProductStatus {
            val days = calculateDaysRemaining(expiryDateStr)
            return when {
                days > 30 -> SAFE
                days in 1..30 -> WARNING
                days == 0L -> TODAY
                else -> EXPIRED
            }
        }

        fun getDaysRemainingText(days: Long): String {
            return when {
                days > 1 -> "Sisa $days hari lagi"
                days == 1L -> "Sisa 1 hari lagi"
                days == 0L -> "Kedaluwarsa Hari Ini!"
                days == -1L -> "Kedaluwarsa 1 hari lalu"
                else -> "Kedaluwarsa ${Math.abs(days)} hari lalu"
            }
        }

        private fun getStartOfDay(date: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.time
        }
    }
}