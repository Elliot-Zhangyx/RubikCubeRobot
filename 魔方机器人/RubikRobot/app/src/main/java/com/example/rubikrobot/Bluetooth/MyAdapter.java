package com.example.rubikrobot.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.rubikrobot.R;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends ArrayAdapter<DeviceInfo> {

    public MyAdapter(Context context, ArrayList<DeviceInfo> deviceInfos)
    {
        super(context,0, deviceInfos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Get the data item for this position
        DeviceInfo deviceInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_name, parent, false);
        }

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.name);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.address);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        // Populate the data into the template view suing the data object
        tvName.setText(deviceInfo.name);
        tvAddress.setText(deviceInfo.address);
        image.setImageResource(deviceInfo.imageId);

        // return the complete view to render on screen
        return  convertView;
    }
}
