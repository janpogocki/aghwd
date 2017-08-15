package pl.janpogocki.agh.wirtualnydziekanat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String typeKey = data.get("type");

            if (typeKey.equals("new_mark")){
                MarksMessageNotification.notify(this);
            }
            else if (typeKey.equals("news")){
                String text = data.get("text");
                String URL = data.get("url");
                BigViewMessageNotification.notify(this, text, URL);
            }
        }
    }
}
