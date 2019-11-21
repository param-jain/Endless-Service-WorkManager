package com.robertohuertas.endless

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log

import androidx.work.Data
import androidx.work.Worker

import android.content.Context.NOTIFICATION_SERVICE

class MyWorker : Worker() {

    override fun doWork(): Result {
        //Executed on different thread

        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val title = getInputData().getString(EXTRA_TITLE, "Default Title")
        val text = getInputData().getString(EXTRA_TEXT, "Default Text")

        //sendNotification("Simple Work Manager", "I have been send by WorkManager!");
        if (title != null) {
            if (text != null) {
                sendNotification(title, text)
            }
        }

        val output = Data.Builder()
            .putString(EXTRA_OUTPUT_MESSAGE, "I have come from MyWorker!")
            .build()

        setOutputData(output)

        return Result.SUCCESS
    }

    fun sendNotification(title: String, message: String) {
        val notificationManager =
            getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(getApplicationContext(), "default")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)

        notificationManager.notify(1, notification.build())
    }

    companion object {

        val EXTRA_TITLE = "title"
        val EXTRA_TEXT = "text"
        val EXTRA_OUTPUT_MESSAGE = "output_message"
    }
}
