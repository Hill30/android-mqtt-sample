package com.hill30.android;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.net.URISyntaxException;

public class MQTTService extends IntentService {

    public final static String BROKER_URL = "org.example.mqtt.BROKER_URL";
    public final static String BROKER_TOPIC = "org.example.mqtt.BROKER_TOPIC";
    public final static String USER_NAME = "org.example.mqtt.USER_NAME";
    public final static String USER_PSWD = "org.example.mqtt.USER_PSWD";
    public final static String BROADCAST_ACTION = "org.example.mqtt.BROADCAST";
    public final static String BROADCAST_MSG = "org.example.mqtt.BROADCAST_MSG";
    public final static String CLIENT_ID = "org.example.mqtt.CLIENT_ID";

    private final String TAG = "MQTTListener";
    private String brokerAddress = "tcp://10.0.2.2:1883";
    private String topic_name = "TestOne";
    private String sUserName = "";
    private String sPassword = "";
    private String sClientId = "android-mqtt-svc";
    private CallbackConnection listenerConnection = null;
    private MQTT mqtt = null;

    public MQTTService() {
        super("MQTTService");
    }

    private void SetConnectionDetails (Intent intent)
    {
        String brokerURL = intent.getStringExtra(BROKER_URL);
        if(brokerURL != null && !brokerURL.isEmpty())
            brokerAddress = brokerURL;

        String topic = intent.getStringExtra(BROKER_TOPIC);
        if(topic != null && !topic.isEmpty())
            topic_name = topic;

        String userName = intent.getStringExtra(USER_NAME);
        if(userName != null && !userName.isEmpty())
            sUserName = userName;

        String pswd = intent.getStringExtra(USER_PSWD);
        if(pswd != null && !pswd.isEmpty())
            sPassword = pswd;

        String id = intent.getStringExtra(CLIENT_ID);
        if(id != null && !id.isEmpty())
            sClientId = id;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        SetConnectionDetails(intent);

        // automatically connect if no longer connected
        if(listenerConnection == null)
        {
            connect();
        }

        try
        {
            // Wait forever..
            synchronized (Listener.class) {
                while(true)
                    Listener.class.wait();
            }
        }
        catch(Exception exc)
        {
            Log.e(TAG, "URISyntaxException connecting to " + brokerAddress + " - " + exc);
        }
    }

    private void connect()
    {
        mqtt = new MQTT();
        mqtt.setClientId(sClientId);

        try
        {
            mqtt.setHost(brokerAddress);
            Log.d(TAG, "Address set: " + brokerAddress);


            if(sUserName != null && !sUserName.equals(""))
            {
                mqtt.setUserName(sUserName);
                Log.d(TAG, "UserName set: [" + sUserName + "]");
            }

            if(sPassword != null && !sPassword.equals(""))
            {
                mqtt.setPassword(sPassword);
                Log.d(TAG, "Password set: [" + sPassword + "]");
            }

            mqtt.setCleanSession(false); ///AAB for durable topic
            listenerConnection = mqtt.callbackConnection();
        }
        catch(URISyntaxException urise)
        {
            Log.e(TAG, "URISyntaxException connecting to " + brokerAddress + " - " + urise);
        }
        catch(Exception exc)
        {
            Log.e(TAG, "Exception: " + exc.getMessage());
        }

        listenerConnection.listener(new org.fusesource.mqtt.client.Listener() {
            long count = 0;
            long start = System.currentTimeMillis();

            public void onConnected() {
            }
            public void onDisconnected() {
            }
            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }
            public void onPublish(UTF8Buffer topic, Buffer msg, Runnable ack) {
                String body = msg.utf8().toString();
                if( "SHUTDOWN".equals(body)) {
                    long diff = System.currentTimeMillis() - start;
                    Log.d(TAG, String.format("Received %d in %.2f seconds", count, (1.0*diff/1000.0)));
                    listenerConnection.disconnect(new Callback<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            System.exit(0);
                        }
                        @Override
                        public void onFailure(Throwable value) {
                            value.printStackTrace();
                            System.exit(-2);
                        }
                    });
                } else {
                    if( count == 0 ) {
                        start = System.currentTimeMillis();
                    }
                    //if( count % 1000 == 0 ) {
                    //    Log.d(TAG, String.format("Received %d messages.", count));
                    //}
                    String messagePayLoad = new String(msg.getData());
                    Log.d(TAG, String.format("Received %d. Message: %s.", count, messagePayLoad));
                    reportMessage( String.format("Received %d. Message: %s.", count, messagePayLoad) );
                    count ++;
                }
                ack.run();
            }
        });
        listenerConnection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Topic[] topics = {new Topic(topic_name, QoS.AT_LEAST_ONCE)};
                listenerConnection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        Log.d(TAG, String.format("Subscribed to topic: %s.", topic_name));
                    }

                    public void onFailure(Throwable value) {
                        value.printStackTrace();
                        System.exit(-2);
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }
        });
    }

    private void reportMessage(String msg)
    {
        // report message back to listeners
        /*
     * Creates a new Intent containing a Uri object
     * BROADCAST_ACTION is a custom Intent action
     */
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(BROADCAST_MSG, msg);
        // Broadcasts the Intent to receivers in this app.
        sendBroadcast(localIntent);
    }
}