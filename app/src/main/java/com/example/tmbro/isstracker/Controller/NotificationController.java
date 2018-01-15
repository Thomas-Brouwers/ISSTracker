package com.example.tmbro.isstracker.Controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.example.tmbro.isstracker.R;
import com.example.tmbro.isstracker.View.MapActivity;

/**
 * Created by tmbro on 15-1-2018.
 */

public class NotificationController {
Context context;
    public NotificationController(Context context) {
        // Intent to start the main Activity
        this.context = context;
        Intent notificationIntent = new Intent(context.getApplicationContext(), NotificationController.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MapActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificatioMng.notify(
                0,
                createNotification(context.getString(R.string.close_toast), "", notificationPendingIntent));

    }

    // Create notification
    private Notification createNotification(String msg, String name, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "ISS");
        notificationBuilder
                .setSmallIcon(R.drawable.space_station)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText(name)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }
}
