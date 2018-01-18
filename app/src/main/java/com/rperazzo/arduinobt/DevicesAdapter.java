package com.rperazzo.arduinobt;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DevicesAdapter extends BaseAdapter {

    private List<BluetoothDevice> devices;

    public DevicesAdapter() {
        devices = new ArrayList<>();
    }

    public void add(BluetoothDevice device){
        devices.add(device);
    }

    public boolean exists(BluetoothDevice device){

        boolean exist = false;

        for (int i = 0; i < devices.size(); i++) {
            if(devices.get(i).getAddress().equals(device.getAddress())){
                exist = true;
                break;
            }
        }

        return exist;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){


            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, null);

            holder = new ViewHolder();
            holder.txtName = (TextView)convertView.findViewById(android.R.id.text1);
            holder.txtMac = (TextView)convertView.findViewById(android.R.id.text2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = (BluetoothDevice)getItem(position);

        holder.txtName.setText(device.getName());
        holder.txtMac.setText(device.getAddress());

        return convertView;
    }

    private static class ViewHolder{
        TextView txtName;
        TextView txtMac;
    }

}
