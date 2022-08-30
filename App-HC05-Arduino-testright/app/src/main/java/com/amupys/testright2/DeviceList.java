package com.amupys.testright2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity {

    ListView devicelist, availableDevices;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    ArrayList<String> list_available;
    public static String EXTRA_ADDRESS = "device_address";

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(700);

        devicelist = (ListView) findViewById(R.id.listView);
        availableDevices = (ListView) findViewById(R.id.list_avail);
        ImageView scan_btn = findViewById(R.id.btn_scan);
        scan_btn.setOnClickListener(view -> {
            pairedDevicesList();
            scan_btn.startAnimation(anim);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scan_btn.setAnimation(null);
                    Toast.makeText(DeviceList.this, "Refreshed", Toast.LENGTH_SHORT).show();
                }
            }, 500);
        });

        findBluetoothDevices();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        ImageView available_btn = findViewById(R.id.btn_scan_available);
        available_btn.setOnClickListener(view -> {
            if (myBluetooth.isDiscovering())
                myBluetooth.cancelDiscovery();

            if (myBluetooth.startDiscovery()) {
                available_btn.startAnimation(anim);

                list_available = new ArrayList<>();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        available_btn.setAnimation(null);
                        Toast.makeText(DeviceList.this, "Refreshed", Toast.LENGTH_SHORT).show();
                    }
                }, 500);
            } else
                Toast.makeText(this, "Error in finding bluetooth devices. Enable device location and try again", Toast.LENGTH_SHORT).show();
        });

        pairedDevicesList();
    }

    @SuppressLint("MissingPermission")
    private void findBluetoothDevices() {
        list_available = new ArrayList<>();
        if (myBluetooth.isDiscovering())
            myBluetooth.cancelDiscovery();

        myBluetooth.startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void pairedDevicesList() {
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        } else {
            fillList();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(DeviceList.this,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(DeviceList.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    return;
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

//                Log.e("onReceive", deviceName + ", " + deviceHardwareAddress);

                list_available.add(deviceName + "\n" + deviceHardwareAddress);

                final ArrayAdapter adapter = new ArrayAdapter(DeviceList.this,
                        android.R.layout.simple_list_item_1, list_available);
                availableDevices.setAdapter(adapter);
                availableDevices.setOnItemClickListener(myListClickListener);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void fillList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

//        Log.e("list", String.valueOf(pairedDevices.size()));

        if ( pairedDevices.size() > 0 ) {
            for ( BluetoothDevice bt : pairedDevices ) {
                list.add(bt.getName().toString() + "\n" + bt.getAddress().toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            fillList();
        }
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);

            Intent intent=new Intent();
            intent.putExtra(EXTRA_ADDRESS, address);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}
