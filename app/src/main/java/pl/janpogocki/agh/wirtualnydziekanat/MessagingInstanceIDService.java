package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import pl.janpogocki.agh.wirtualnydziekanat.javas.RememberPassword;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

/**
 * Created by Jan on 02.10.2017.
 */

public class MessagingInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        /*try {
            RememberPassword rp = new RememberPassword(this);

            if (rp.isRemembered()) {
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                if (sharedPreferences.getBoolean("marks_notifications", true)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(rp.getLogin());
                }
            }
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }*/
    }
}