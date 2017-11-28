package net.tijos.gas;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.tijos.gas.service.SentryService;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends Activity implements SentryService.AlarmListener, SentryService.ChangedListener, View.OnClickListener {

    private SentryService sentry;

    private ImageView iv_logo;
    private LinearLayout layout_alarm;
    private TextView tv_alarm;
    private TextView tv_temp;
    private TextView tv_humi;
    private Button btn_silent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.err.println("onCreate");

        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        layout_alarm = (LinearLayout) findViewById(R.id.layout_alarm);
        tv_alarm = (TextView) findViewById(R.id.tv_alarm);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_humi = (TextView) findViewById(R.id.tv_humi);
        btn_silent = (Button) findViewById(R.id.btn_silent);
        btn_silent.setOnClickListener(this);


        MyApplication app = (MyApplication) getApplication();
        sentry = app.getSentry();
        sentry.registerAlarmListener(this);
        sentry.registerChangedListener(this);

        if (app.getSentry().isAlarm()) {
            iv_logo.setImageResource(R.drawable.warning);
            layout_alarm.setBackgroundColor(Color.parseColor("#ffcc0000"));
            tv_alarm.setText("警告");
            btn_silent.setEnabled(true);
        } else {
            iv_logo.setImageResource(R.drawable.security);
            layout_alarm.setBackgroundColor(Color.parseColor("#ff669900"));
            tv_alarm.setText("安全");
            btn_silent.setEnabled(false);
        }

        tv_humi.setText(app.getSentry().getHumidity() + "%");
        tv_temp.setText(app.getSentry().getTemperature() + "℃");
    }


    @Override
    protected void onDestroy() {
        System.err.println("onDestroy");

        sentry.unregisterAlarmListener(this);
        sentry.unregisterChangedListener(this);

        super.onDestroy();
    }

    @Override
    public void onAlarm() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_logo.setImageResource(R.drawable.warning);
                layout_alarm.setBackgroundColor(Color.parseColor("#ffcc0000"));
                tv_alarm.setText("警告");
                btn_silent.setEnabled(true);
            }
        });

    }

    @Override
    public void onRecovery() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_logo.setImageResource(R.drawable.security);
                layout_alarm.setBackgroundColor(Color.parseColor("#ff669900"));
                tv_alarm.setText("安全");
                btn_silent.setEnabled(false);
            }
        });

    }

    @Override
    public void onHumidityChanged(final double humi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_humi.setText(humi + "%");
            }
        });
    }

    @Override
    public void onTemperatureChanged(final double temp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_temp.setText(temp + "℃");
            }
        });

    }

    @Override
    public void onClick(View v) {
        try {
            sentry.buzzerOff();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
