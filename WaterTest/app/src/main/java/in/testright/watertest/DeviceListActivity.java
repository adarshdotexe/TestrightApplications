package in.testright.watertest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    public static String EXTRA_ADDRESS = "device_address";
    private final AdapterView.OnItemClickListener myListClickListener = (parent, view, position, id) -> {
        String info = ((TextView) view).getText().toString();
        String address = info.substring(info.length() - 17);

        Intent intent = new Intent();
        intent.putExtra(EXTRA_ADDRESS, address);
        setResult(11, intent);
        finish();
    };
    ListView devicelist, availableDevices;
    ArrayList<String> list_available;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(DeviceListActivity.this,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DeviceListActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    return;
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                list_available.add(deviceName + "\n" + deviceHardwareAddress);

                final ArrayAdapter<String> adapter = new ArrayAdapter<>(DeviceListActivity.this,
                        android.R.layout.simple_list_item_1, list_available);
                availableDevices.setAdapter(adapter);
                availableDevices.setOnItemClickListener(myListClickListener);
            }
        }
    };
    private BluetoothAdapter myBluetooth = null;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fillList();
                }
            });

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

        devicelist = findViewById(R.id.listView);
        availableDevices = findViewById(R.id.list_avail);
        ImageView scan_btn = findViewById(R.id.btn_scan);
        scan_btn.setOnClickListener(view -> {
            pairedDevicesList();
            scan_btn.startAnimation(anim);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                scan_btn.setAnimation(null);
                Toast.makeText(DeviceListActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
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


                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    available_btn.setAnimation(null);
                    Toast.makeText(DeviceListActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
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
            someActivityResultLauncher.launch(turnBTon);
        } else {
            fillList();
        }
    }

    @SuppressLint("MissingPermission")
    private void fillList() {
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

//        Log.e("list", String.valueOf(pairedDevices.size()));

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

}
