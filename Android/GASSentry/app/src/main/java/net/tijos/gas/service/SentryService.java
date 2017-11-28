package net.tijos.gas.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;

import net.tijos.gas.MainActivity;
import net.tijos.gas.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Mars on 2017/10/24.
 */

public class SentryService extends Service implements MqttCallback {

    private boolean isConnect = false;
    private boolean isAlarm = false;

    private static final int NOTIFY_ID = 1;

    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String[] TOPICS = new String[]{"Alarm", "Temperature", "Humidity"};

    private MqttClient client;

    private double lastTemperature = 0;
    private double lastHumidity = 0;

    private List<AlarmListener> alarmListeners = new ArrayList<>();
    private List<ChangedListener> changedListeners = new ArrayList<>();



    public interface AlarmListener {
        void onAlarm();
        void onRecovery();
    }

    public interface ChangedListener {
        void onHumidityChanged(double humi);
        void onTemperatureChanged(double temp);
    }

    public void registerAlarmListener(AlarmListener listener) {
        alarmListeners.add(listener);
    }

    public void unregisterAlarmListener(AlarmListener listener) {
        alarmListeners.remove(listener);
    }

    public void registerChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void unregisterChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }

    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public SentryService getService() {
            return SentryService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.err.println("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.err.println("onStartCommand");


        return START_STICKY;
    }


    public boolean isConnect() {
        return client.isConnected() ? isConnect : false;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public boolean connect(String broker, String username, String passwprd) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        client = new MqttClient(broker, CLIENT_ID, persistence);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(username);
        options.setPassword(passwprd.toCharArray());
        client.setCallback(this);
        client.connect(options);
        client.subscribe(TOPICS);


        isConnect = true;
        return true;
    }

    public void buzzerOff() throws MqttException {
        if (client != null) {
            MqttMessage message = new MqttMessage();
            JSONObject json = new JSONObject();
            json.put("requestId", UUID.randomUUID().toString());
            json.put("Buzzer", "off");
            json.put("Timer", System.currentTimeMillis());

            message.setPayload(json.toJSONString().getBytes());
            message.setQos(1);
            message.setRetained(false);

            client.publish("Action", message);
        }
    }



    @Override
    public void onDestroy() {

        System.err.println("onDestroy");

        isConnect = false;
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.err.println(topic + " - " + new String(message.getPayload()));

        JSONObject json = (JSONObject) JSONObject.parse(message.getPayload());
        if (topic.equals("Alarm")) {

            isAlarm = json.getBooleanValue("Alarm");
            Date date = json.getDate("Timer");
            System.err.println(date);

            if (isAlarm) {
                notifyAlarm();

                for (AlarmListener listener : alarmListeners) {
                    listener.onAlarm();
                }
            }else {
                cancelAlarm();

                for (AlarmListener listener : alarmListeners) {
                    listener.onRecovery();
                }
            }
        }else if (topic.equals("Temperature")) {
            lastTemperature = json.getDoubleValue("Temperature");

            for (ChangedListener listener : changedListeners) {
                listener.onTemperatureChanged(lastTemperature);
            }
        }else if (topic.equals("Humidity")) {
            lastHumidity = json.getDoubleValue("Humidity");
            for (ChangedListener listener : changedListeners) {
                listener.onHumidityChanged(lastHumidity);
            }
        }

        notifyChange();

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    public double getTemperature() {

        return lastTemperature;
    }

    public double getHumidity() {
        return lastHumidity;
    }


    private void notifyChange() {
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder//.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("Sentry working!") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("Listening...\nTemp: " + lastTemperature + "℃\nHumi: " + lastHumidity + "%") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        builder.setStyle(new Notification.BigTextStyle().bigText("Listening...\nTemp: " + lastTemperature + "℃\nHumi: " + lastHumidity + "%"));
        Notification notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_ALL; //设置为默认的声音
        startForeground(NOTIFY_ID, notification);// 开始前台服务
    }

    private void notifyAlarm() {
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("Sentry working!") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("Alarm") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_ALL; //设置为默认的声音

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(0, notification);
    }

    private void cancelAlarm() {
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(0);
    }

}
