package com.example.expiretrack.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.expiretrack.MainActivity
import com.example.expiretrack.R
import com.example.expiretrack.data.model.ProductEntity
import com.example.expiretrack.data.model.ProductStatus

object NotificationHelper {

    const val CHANNEL_ID = "expiretrack_channel_id"
    private const val CHANNEL_NAME = "Pengingat Kedaluwarsa ExpireTrack"
    private const val CHANNEL_DESC = "Notifikasi pengingat untuk produk yang mendekati atau sudah kedaluwarsa"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkAndSendExpiringNotifications(context: Context, products: List<ProductEntity>) {
        try {
            val urgentProducts = products.filter {
                val status = it.getStatus()
                status == ProductStatus.WARNING || status == ProductStatus.TODAY || status == ProductStatus.EXPIRED
            }

            if (urgentProducts.isEmpty()) return

            val warningCount = urgentProducts.count { it.getStatus() == ProductStatus.WARNING }
            val todayCount = urgentProducts.count { it.getStatus() == ProductStatus.TODAY }
            val expiredCount = urgentProducts.count { it.getStatus() == ProductStatus.EXPIRED }

            val title = "Pengingat ExpireTrack ⏰"
            val message = StringBuilder()

            if (todayCount > 0) {
                message.append("• $todayCount produk kedaluwarsa HARI INI!\n")
            }
            if (warningCount > 0) {
                message.append("• $warningCount produk mendekati kedaluwarsa dalam 30 hari.\n")
            }
            if (expiredCount > 0) {
                message.append("• $expiredCount produk telah kedaluwarsa.\n")
            }
            message.append("Buka aplikasi untuk memeriksa daftar lengkap produk Anda.")

            showNotification(context, title, message.toString().trim(), notificationId = 2001)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendTestNotification(context: Context) {
        showNotification(
            context,
            "Notifikasi Uji Coba ExpireTrack 🎉",
            "Sistem notifikasi pengingat tanggal kedaluwarsa berfungsi dengan baik!",
            notificationId = 9999
        )
    }
}