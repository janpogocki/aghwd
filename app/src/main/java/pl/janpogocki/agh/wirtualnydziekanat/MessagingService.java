package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            if (data.containsKey("new_mark")){
                MarksMessageNotification.notify(this);
            }
            else if (data.containsKey("news")){
                String text = data.get("text");
                String URL = data.get("url");
                BigViewMessageNotification.notify(this, text, URL);
            }
        }
    }

    @Override
    public void onNewToken(String s) {
        try {
            RememberPassword rp = new RememberPassword(this);

            if (rp.isRemembered()) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                if (sharedPreferences.getBoolean("marks_notifications", true)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(rp.getLogin());
                }

                if (sharedPreferences.getBoolean("news_notifications", true)) {
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                }
            }
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }
    }
}
