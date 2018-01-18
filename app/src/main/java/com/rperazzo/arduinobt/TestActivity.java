package com.rperazzo.arduinobt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.rperazzo.arduinobt.R;

import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

// Amarino Activity
public class TestActivity extends AppCompatActivity {

    // Modify Bluetooth MAC address before executing
    //private static final String ARDUINO_ADDRESS = "00:11:09:01:06:39";
    private String ARDUINO_ADDRESS;

    private Spinner mSpinner;
    private EditText mEditText;

    // Activity functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mSpinner = (Spinner) findViewById(R.id.spinner2);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.flags_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(0);

        mEditText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        if (intent != null) {
            ARDUINO_ADDRESS = intent.getStringExtra(BluetoothActivity.MAC_ADDRESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_connect) {
            connect();
            return true;
        } else if (id == R.id.action_disconnect) {
            disconnect();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Do something on receiving data from Arduino
    private void onDataReceived(String address, int dataType, Intent intent) {

        // Example: If is a string, show as a toast
        if (dataType == AmarinoIntent.STRING_EXTRA) {
            String data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

            if (data != null) {
                showMessage(data);
            }
        }
    }

    // On click, send data to Arduino
    public void onClickButton(View view) {
        // Example: Send one string using flag "A"
        Amarino.sendDataToArduino(getApplicationContext(), ARDUINO_ADDRESS,'A',"TESTE DE ENVIO");
    }

    public void onSendButton(View view) {
        String flag = (String) mSpinner.getSelectedItem();
        char flagChar = flag.charAt(flag.length()-1);
        String data = mEditText.getText().toString();
        Amarino.sendDataToArduino(getApplicationContext(), ARDUINO_ADDRESS,flagChar,data);
        mEditText.setText("");
    }

    // Functions to manage Arduino connection
    private ConnectionReceiver mConnectionReceiver = null;
    public class ConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (AmarinoIntent.ACTION_CONNECTED.equals(intent.getAction())) {
                showMessage("CONNECTED");
                registerDataReceiver();
            } else {
                showMessage("DISCONNECTED");
                unregisterDataReceiver();
            }
        }
    }

    public void connect() {
        registerConnectionReceiver();
        Amarino.connect(getApplicationContext(), ARDUINO_ADDRESS);
    }

    public void disconnect() {
        Amarino.disconnect(getApplicationContext(), ARDUINO_ADDRESS);
    }

    private void registerConnectionReceiver() {
        if (mConnectionReceiver == null) {
            mConnectionReceiver = new ConnectionReceiver();
        }
        IntentFilter actionsFilter = new IntentFilter();
        actionsFilter.addAction(AmarinoIntent.ACTION_CONNECTED);
        actionsFilter.addAction(AmarinoIntent.ACTION_DISCONNECTED);
        actionsFilter.addAction(AmarinoIntent.ACTION_CONNECTION_FAILED);
        actionsFilter.addAction(AmarinoIntent.ACTION_PAIRING_REQUESTED);
        getApplicationContext().registerReceiver(mConnectionReceiver, actionsFilter);
    }

    // Functions to receive data from Arduino
    private DataReceiver mDataReceiver = null;
    public class DataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
            final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
            onDataReceived(address, dataType, intent);

        }
    }

    private void registerDataReceiver() {
        if (mDataReceiver == null) {
            mDataReceiver = new DataReceiver();
        }
        IntentFilter actionsFilter = new IntentFilter();
        actionsFilter.addAction(AmarinoIntent.ACTION_RECEIVED);
        getApplicationContext().registerReceiver(mDataReceiver, actionsFilter);
    }

    private void unregisterDataReceiver() {
        if (mDataReceiver != null) {
            getApplicationContext().unregisterReceiver(mDataReceiver);
        }
    }

    // Helper functions
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
