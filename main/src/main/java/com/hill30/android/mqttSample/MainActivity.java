package com.hill30.android.mqttSample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hill30.android.mqtt.Service;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {

    private EditText address;
    private ArrayList<Visit> visits;
    private ArrayAdapter<Visit> visitsAdapter;

    private Gson gson;
    private Visit selected;
    private EditText startTime;
    private EditText endTime;
    private SimpleDateFormat dateFormat;
    private View send;

    public Visit visitFromJson(String json) {
        try {
            return gson.fromJson((String) json, Visit.class);
        } catch (Exception ex) {
            return new Visit();
        }
    }

    class Visit {

        public int id;
        public Date startTime;
        public Date endTime;

        @Override
        public String toString() {
            return "(" + id + ") " + startTime.toLocaleString();
        }
    }

    class Watcher implements TextWatcher {

        private final EditText control;

        public Watcher(EditText control) {
            this.control = control;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                if (!editable.toString().isEmpty())
                    dateFormat.parse(editable.toString());
                send.setEnabled(true);
                control.setTextColor(Color.BLACK);
            } catch (ParseException e) {
                send.setEnabled(false);
                control.setTextColor(Color.RED);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address = (EditText)findViewById(R.id.address);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        startTime = (EditText)findViewById(R.id.startTime);
        startTime.addTextChangedListener(new Watcher(startTime));
        endTime = (EditText)findViewById(R.id.endTime);
        endTime.addTextChangedListener(new Watcher(endTime));

        JsonSerializer<Date> ser = new JsonSerializer<Date>() {
            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext
                    context) {
                return src == null ? null : new JsonPrimitive(dateFormat.format(src));
            }
        };

        JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
                try {
                    return json == null ? null : dateFormat.parse(json.getAsString());
                }
                catch(ParseException ex){
                    throw new JsonParseException(ex);
                }
            }
        };

        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, ser)
                .registerTypeAdapter(Date.class, deser).create();

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mServiceIntent = new Intent(MainActivity.this, Service.class);
                //mServiceIntent.putExtra(MQTTService.BROKER_URL, "tcp://10.0.2.2:1883");
                //mServiceIntent.putExtra(MQTTService.BROKER_TOPIC, "TestOne");
                //mServiceIntent.putExtra(MQTTService.CLIENT_ID, "android_svc");

                startService(mServiceIntent);
            }
        });

        send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (startTime.getText().toString().isEmpty())
                    selected.startTime = null;
                else
                    try {
                        selected.startTime = dateFormat.parse(startTime.getText().toString());
                    } catch (ParseException e) {}

                if (endTime.getText().toString().isEmpty())
                    selected.endTime = null;
                else
                    try {
                        selected.endTime = dateFormat.parse(endTime.getText().toString());
                    } catch (ParseException e) {}
                String message = gson.toJson(selected);
                Intent intent = new Intent(Service.SEND_MESSAGE);
                intent.putExtra(Service.MESSAGE_PAYLOAD, message);
                sendBroadcast(intent);
            }
        });

        visits = new ArrayList<Visit>();

        ListView visits = (ListView) findViewById(R.id.visits);
        visitsAdapter = new ArrayAdapter<Visit>(this, android.R.layout.simple_list_item_1, MainActivity.this.visits);
        visits.setAdapter(visitsAdapter);

        visits.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                selected = (Visit) parent.getItemAtPosition(position);
                if (selected.startTime == null)
                    startTime.setText("");
                else
                    startTime.setText(dateFormat.format(selected.startTime));
                if (selected.endTime == null)
                    endTime.setText("");
                else
                    endTime.setText(dateFormat.format(selected.endTime));
            }

        });

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainActivity.this.visits.add(visitFromJson(intent.getCharSequenceExtra(Service.MESSAGE_PAYLOAD).toString()));
                visitsAdapter.notifyDataSetChanged();
            }
        }, new IntentFilter(Service.MESSAGE_RECEIVED));

    }

}
