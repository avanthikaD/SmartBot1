package com.softwareproject.SmartBot;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class RingtonePlayingService extends Service {
    private static final String TAG = RingtonePlayingService.class.getSimpleName();
    private static final String URI_BASE = RingtonePlayingService.class.getName() + ".";
    public static final String ACTION_DISMISS = URI_BASE + "ACTION_DISMISS";

    private Ringtone r;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent == null) {
            Log.d(TAG, "The intent is null.");
            return START_REDELIVER_INTENT;
        }
        String action = intent.getAction();

        if (ACTION_DISMISS.equals(action))
            dismissRingtone();
        else {
            Vibrator v = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
            v.vibrate(4000);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r =RingtoneManager.getRingtone(this,notification);
            r.play();
        }
        return START_NOT_STICKY;
    }
    public void dismissRingtone() {
        // stop the alarm ringtone
        Intent i = new Intent(this, RingtonePlayingService.class);
        stopService(i);

        // also dismiss the alarm to ring again or trigger again
        AlarmManager aManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getBaseContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        aManager.cancel(pendingIntent);

        // Canceling the current notification
        NotificationManager notificationManager = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.cancel(123);
    }
    @Override
    public void onDestroy() {
        r.stop();
    }
}

