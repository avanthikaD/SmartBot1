package com.softwareproject.SmartBot;


import android.app.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;


public class AlertReceiver extends BroadcastReceiver {

    public static final String channelID = "ID";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // using service class
        Intent i = new Intent(context, RingtonePlayingService.class);
        context.startService(i);
        createNotification(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  void createNotification(Context context) {

        NotificationChannel notificationChannel=new NotificationChannel(channelID,"Alarm",NotificationManager.IMPORTANCE_HIGH);

        Builder builder = new Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm Notification")
                .setContentText("your alarm is ringing")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        //To add a dismiss button
        Intent dismissIntent = new Intent(context, RingtonePlayingService.class);
        dismissIntent.setAction(RingtonePlayingService.ACTION_DISMISS);

        PendingIntent pendingIntent = PendingIntent.getService(context, 123, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(android.R.drawable.ic_lock_idle_alarm, "DISMISS", pendingIntent);
        builder.addAction(action);
        // end of setting action button to notification

        Intent intent1 = new Intent(context, HomeActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 123, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(123, notification);

    }

}



