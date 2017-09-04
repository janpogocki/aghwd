package pl.janpogocki.agh.wirtualnydziekanat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class BigViewMessageNotification {
    private static final String NOTIFICATION_TAG = "AGHBigViewMessage";

    public static void notify(final Context context, final String text, final String URL) {
        final Resources res = context.getResources();

        final String title = res.getString(R.string.app_full_name);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{200, 200, 200, 200, 200, 200})

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_agh_notification)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(Intent.ACTION_VIEW, Uri.parse(URL)),
                                PendingIntent.FLAG_UPDATE_CURRENT))

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 1, notification);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 1);
    }
}
