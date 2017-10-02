package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class MarksMessageNotification {
    private static final String NOTIFICATION_TAG = "AGHMarksMessage";

    public static void notify(final Context context) {
        final Resources res = context.getResources();

        final String title = res.getString(R.string.notification_new_mark_title);
        final String text = res.getString(R.string.notification_new_mark_text);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "urgent")

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{200, 200, 200, 200, 200, 200})
                .setLights(Color.RED, 1000, 2000)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_agh_notification)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, LogIn.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel("urgent", "Pilne", NotificationManager.IMPORTANCE_HIGH);
            nc.setVibrationPattern(new long[]{200, 200, 200, 200, 200, 200});
            nc.setLightColor(context.getResources().getColor(R.color.colorPrimary));
            nm.createNotificationChannel(nc);
        }

        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }
}
