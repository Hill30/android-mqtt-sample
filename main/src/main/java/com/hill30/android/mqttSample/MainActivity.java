package com.hill30.android.mqttSample;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    private EditText address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address = (EditText)findViewById(R.id.address);

        ((Button)findViewById(R.id.connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address.setText("ha");
            }
        });

    }

}
