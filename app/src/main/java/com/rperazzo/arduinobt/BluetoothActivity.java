package com.rperazzo.arduinobt;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.rperazzo.arduinobt.R;

import java.util.Set;

public class BluetoothActivity extends ListActivity {

    private static final int ENABLE_BLUETOOTH = 1;

    public static final String MAC_ADDRESS = "mac_address";

    private BluetoothAdapter mBluetoothAdapter;
    private DevicesAdapter mAdapter;

    private DiscoveryReceiver discoveryReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        discoveryReceiver = null;

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
        } else {
            init();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == ENABLE_BLUETOOTH){
            init();
        } else if (resultCode == RESULT_CANCELED && requestCode == ENABLE_BLUETOOTH){
            finish();
        }
    }

    private void init(){

        mAdapter = new DevicesAdapter();

        listPairedDevices();

        discoveryReceiver = new DiscoveryReceiver();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBluetoothAdapter.cancelDiscovery();

        try{
            if(discoveryReceiver != null){
                unregisterReceiver(discoveryReceiver);
                discoveryReceiver = null;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(discoveryReceiver != null){
            unregisterReceiver(discoveryReceiver);
            discoveryReceiver = null;
        }
    }

    private void listPairedDevices(){

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices != null && pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {

                if(device != null){
                    addDevice(device);
                }
            }
        }
        setListAdapter(mAdapter);
    }

    private class DiscoveryReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //if(deviceEncontrado.getBluetoothClass()
                //        .getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE){
                    addDevice(device);
                //}
            }
        }
    }

    private void addDevice(BluetoothDevice device){
        if(!mAdapter.exists(device)){

            mAdapter.add(device);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        BluetoothDevice device = (BluetoothDevice) l.getItemAtPosition(position);

        Intent activity = new Intent(this, TestActivity.class);
        activity.putExtra(MAC_ADDRESS, device.getAddress());
        startActivity(activity);
    }
}
