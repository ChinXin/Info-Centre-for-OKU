package my.edu.tarc.oku

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.graphics.BitmapFactory
import android.media.RingtoneManager


class ReminderBroadcast: BroadcastReceiver() {
    @SuppressLint("ResourceType")
    override fun onReceive(context: Context, intent: Intent) {
        val reminderMessage = intent.extras!!.getString("remindMessage")
        val byteArray = intent.extras!!.getByteArray("remindImage")
        val reminderImage = BitmapFactory.decodeByteArray(byteArray,0, byteArray!!.size)
        val reminderRequestCode = intent.extras!!.getInt("remindRequestCode")
        val notificationSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder:NotificationCompat.Builder = NotificationCompat.Builder(context, "notifyEvent")
            .setSmallIcon(R.drawable.disabled_person)
            .setAutoCancel(true)
            .setContentTitle("OKU")
            .setContentText(reminderMessage)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(reminderMessage))
            .setLargeIcon(reminderImage)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(reminderImage)
                .bigLargeIcon(null))
            .setSound(notificationSound)

        val notificationManager:NotificationManagerCompat = NotificationManagerCompat.from(context)

        notificationManager.notify(reminderRequestCode, builder.build())
    }
}