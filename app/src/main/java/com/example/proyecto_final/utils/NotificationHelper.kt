package com.example.proyecto_final.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyecto_final.MainActivity
import com.example.proyecto_final.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_MAINTENANCE_ID = "maintenance_alerts"
        const val CHANNEL_SPEED_ID = "speed_alerts"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val maintenanceChannel = NotificationChannel(
                CHANNEL_MAINTENANCE_ID,
                "Alertas de Mantenimiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre mantenimientos vencidos o próximos"
            }

            val speedChannel = NotificationChannel(
                CHANNEL_SPEED_ID,
                "Alertas de Velocidad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de exceso de velocidad"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(maintenanceChannel)
            manager.createNotificationChannel(speedChannel)
        }
    }

    fun showNotification(channelId: String, title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}
