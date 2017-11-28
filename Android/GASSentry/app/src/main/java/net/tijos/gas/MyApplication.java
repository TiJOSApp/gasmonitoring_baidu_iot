package net.tijos.gas;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import net.tijos.gas.service.SentryService;

/**
 * Created by Mars on 2017/10/26.
 */

public class MyApplication extends Application {

    private SentryService sentry;

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            sentry = ((SentryService.LocalBinder)binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        bindService(new Intent(this, SentryService.class), conn, Context.BIND_AUTO_CREATE);


    }


    public SentryService getSentry() {
        return sentry;
    }
}
