package pl.janpogocki.agh.wirtualnydziekanat.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Jan on 16.09.2016.
 */
public class AuthService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Auth authenticator = new Auth(this);
        return authenticator.getIBinder();
    }
}
