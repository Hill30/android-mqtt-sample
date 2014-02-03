package com.hill30.android.paho;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hill30.android.mqttSample.PahoClientActivity;
import com.hill30.android.net.Constants;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;


/*
* Nice implmentation that supports keep alive and reconnect things:
* https://github.com/JesseFarebro/Android-Paho-MQTT-Service/blob/master/src/com/jessefarebro/mqtt/MqttService.java
*
* Useful article on MQTT, Android and Paho client
* http://www.infoq.com/articles/practical-mqtt-with-paho
*
* */

public class MQTTService extends Service {

    public static final String BROKER_URL = "tcp://"+ Constants.ACTIVE_MQ_IP +":1883";
    public static final String clientId = "paho-user-2";
    public static final String TOPIC = "ServiceTracker.Inbound.User";
    private static final String THREAD_NAME = "pahoThread";
    private MqttClient mqttClient;
    private Handler connectionHandler;
    private MqttCallback mqttCallback;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        HandlerThread thread = new HandlerThread(THREAD_NAME);
        thread.start();
        mqttCallback = new MqttPahoCallback();
        connectionHandler = new Handler(thread.getLooper());
        startConnection();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Network connected.", Toast.LENGTH_SHORT).show();
                Log.d("Service", "Network connected");
                startConnection();
            }
        }, new IntentFilter(Constants.INTENT_NETWORK_CONNECTED));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Network not connected.", Toast.LENGTH_SHORT).show();
                Log.d("Service", "No network");
            }
        }, new IntentFilter(Constants.INTENT_NETWORK_DISCONNECTED));
    }

    private void startConnection(){
        connectionHandler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());

                    mqttClient.setCallback(mqttCallback);
                    mqttClient.connect();

                    //Subscribe to all subtopics of homeautomation
                    mqttClient.subscribe(TOPIC);


                } catch (MqttException e) {
                    Log.e("MQTTService", e.getMessage(), e);
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        try {
            mqttClient.disconnect(0);
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    protected class MqttPahoCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable throwable) {
            Log.e("PushCallback", "Connection lost", throwable);
            mqttClient = null;
            startConnection();
        }

        @Override
        public void messageArrived(MqttTopic mqttTopic, MqttMessage mqttMessage) throws Exception {
            Log.d("PushCallback", "Message " + mqttMessage.toString());
            MQTTService.this.sendBroadcast(
                    new Intent(PahoClientActivity.MESSAGE_RECEIVED).putExtra(PahoClientActivity.MESSAGE_PAYLOAD, mqttMessage.toString())
            );
        }

        @Override
        public void deliveryComplete(MqttDeliveryToken mqttDeliveryToken) {

        }
    }
}
