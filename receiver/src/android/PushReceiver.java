package me.pushy.sdk;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;

import me.pushy.sdk.cordova.internal.util.PushyPersistence;

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Notification title and text
        String notificationTitle = getAppName(context);
        String notificationText = "";
        String notificationUrl = "";
        int notId = 1;

        // Attempt to extract the notification text from the "message" property of the data payload
        if (intent.getStringExtra("message") != null) {
            notificationText = intent.getStringExtra("message");
        }

        // Attempt to extract the notification text from the "title" property of the data payload
        if (intent.getStringExtra("title") != null) {
            notificationTitle = intent.getStringExtra("title");
        }

        notId = intent.getIntExtra("notId",1);

        // Prepare a notification with vibration and sound
        Notification.Builder builder = new Notification.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setSmallIcon(getNotificationIcon(context))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(getMainActivityPendingIntent(context));

        //Attempt to extract the notification url
        if(intent.getStringExtra("url") != null) {
            notificationUrl = intent.getStringExtra("url");
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(notificationUrl));
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            builder.setContentIntent(contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Automatically configure a Notification Channel for devices running Android O+
        Pushy.setNotificationChannel(builder, context);

        // Build the notification and display it
        notificationManager.notify(notId, builder.build());
    }

    private int getNotificationIcon(Context context) {
        // Attempt to fetch icon name from SharedPreferences
        String icon = PushyPersistence.getNotificationIcon(context);

        // Did we configure a custom icon?
        if (icon != null) {
            // Cache app resources
            Resources resources = context.getResources();

            // Cache app package name
            String packageName = context.getPackageName();

            // Look for icon in drawable folders
            int iconId = resources.getIdentifier(icon, "drawable", packageName);

            // Found it?
            if (iconId != 0) {
                return iconId;
            }

            // Look for icon in mipmap folders
            iconId = resources.getIdentifier(icon, "mipmap", packageName);

            // Found it?
            if (iconId != 0) {
                return iconId;
            }
        }

        // Fallback to generic icon
        return android.R.drawable.ic_dialog_info;
    }

    private static String getAppName(Context context) {
        // Attempt to determine app name via package manager
        return context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
    }

    private PendingIntent getMainActivityPendingIntent(Context context) {
        // Get launcher activity intent
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getApplicationContext().getPackageName());

        // Make sure to update the activity if it exists
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Convert intent into pending intent
        return PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}