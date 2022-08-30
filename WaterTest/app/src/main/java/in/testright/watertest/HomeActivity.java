package in.testright.watertest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import in.testright.watertest.models.Program;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public ArrayList<Program> programArrayList = new ArrayList<>();
    public BluetoothSocket mmSocket = null;

    String address = null;
    Activity activity = HomeActivity.this;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    MaterialButton bluetoothButton;
    ConnectThread connectThread;
    CardView home;
    CardView list;
    ListView listView;
    Program selectedProgram;
    TextView titleList;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 11) {
                    assert result.getData() != null;
                    address = result.getData().getStringExtra(DeviceListActivity.EXTRA_ADDRESS);
                    connectThread = new ConnectThread(bluetoothAdapter.getRemoteDevice(address));
                    connectThread.start();
                } else if (result.getResultCode() == 12) {
                    fillList();

                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getPermissions();
        initCards();
        initButtons();
        createPrograms();


    }

    private void initCards() {
        home = findViewById(R.id.cardHome);
        list = findViewById(R.id.cardList);
        listView = findViewById(R.id.listViewCardList);
        titleList = findViewById(R.id.textViewList);
        home.setVisibility(View.VISIBLE);
    }


    private void createPrograms() {
        String[] ProgramNames = {"Turbidity", "Color", "Fluoride", "Nitrate", "Ammonia-N", "Iron", "R/Cl2"};
        String[] Units = {"NTU", "PtCo", "mg/l", "mg/l", "mg/l", "mg/l", "mg/l"};


        for (int i = 0; i < ProgramNames.length; i++) {
            programArrayList.add(new Program(i+1, ProgramNames[i], Units[i],
                    preferences.getInt(ProgramNames[i] + "a", 0),
                    preferences.getInt(ProgramNames[i] + "b", 0),
                    preferences.getInt(ProgramNames[i] + "c", 0),
                    preferences.getInt(ProgramNames[i] + "x", 0)));
        }
    }

    private void getPermissions() {
        String[] permissions;
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN};
        } else {
//            android 10 and 11
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        }


        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[0]), 111);
        } else {
            autoConnect();
        }
    }

    @SuppressLint("MissingPermission")
    private void autoConnect() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            setResult(12, turnBTon);
            someActivityResultLauncher.launch(turnBTon);
        } else {
            fillList();
        }
    }



    private void initButtons() {
        ProgramAdapter adapterOption = new ProgramAdapter(getApplicationContext(), activity, programArrayList, ProgramAdapter.TYPE_OPTION);
        ProgramAdapter adapterSetting = new ProgramAdapter(getApplicationContext(), activity, programArrayList, ProgramAdapter.TYPE_SETTING);
        ProgramAdapter adapterTest = new ProgramAdapter(getApplicationContext(), activity, programArrayList, ProgramAdapter.TYPE_TEST);

        MaterialButton newTestButton = findViewById(R.id.materialButtonStart);
        newTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                listView.setAdapter(adapterOption);
            }
        });

        MaterialButton historyButton = findViewById(R.id.materialButtonHistory);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        MaterialButton logoutButton = findViewById(R.id.materialButtonLogout);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });

        MaterialButton infoButton = findViewById(R.id.materialButtonInfo);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        MaterialButton settingsButton = findViewById(R.id.materialButtonSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                ProgramAdapter adapter = new ProgramAdapter(getApplicationContext(), activity, programArrayList, ProgramAdapter.TYPE_OPTION);
                listView.setAdapter(adapter);

            }
        });

        bluetoothButton = findViewById(R.id.materialButtonConnectBluetooth);
        bluetoothButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, DeviceListActivity.class);
            setResult(11, intent);
            someActivityResultLauncher.launch(intent);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                autoConnect();
            } else {
                showSettingsDialog();
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Need Permissions");

        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            dialog.cancel();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @SuppressLint("MissingPermission")
    private void fillList() {
        try {
            boolean flag = false;
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().contains("HC-05")) {
                        address = bt.getAddress();
                        flag = true;
                        connectThread = new ConnectThread(bluetoothAdapter.getRemoteDevice(address));
                        connectThread.start();
                        break;
                    }
                }
                if (!flag) {
                    Toast.makeText(this, "HC-05 Bluetooth device not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                runOnUiThread(() -> Toast.makeText(activity, "Trying to connect to device", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                runOnUiThread(() -> Toast.makeText(activity, "Error Connecting to device", Toast.LENGTH_SHORT).show());
                // Unable to connect; close the socket and return.
                cancel();
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            runOnUiThread(() -> {
                runOnUiThread(() -> Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show());

                bluetoothButton.setText("Disconnect");
                bluetoothButton.setOnClickListener(v -> {
                    if (connectThread != null) {
                        connectThread.cancel();
                    }
                });
                TextView textView = findViewById(R.id.textConnected);
                textView.setText("Connected to " + mmDevice.getName());
            });
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
                runOnUiThread(() -> {
                    bluetoothButton.setText("Connect");
                    bluetoothButton.setOnClickListener(v -> {
                        Intent intent = new Intent(activity, DeviceListActivity.class);
                        setResult(11, intent);
                        someActivityResultLauncher.launch(intent);
                    });
                    TextView textView = findViewById(R.id.textConnected);
                    textView.setText("Disconnected");
                });
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private float processValues(ArrayList<Double> arrayList, Program program) {
        try {
            int inputStart = 1;
            int inputEnd = 8;
            int graphs = 300;
            double[] inputPnt = new double[]{
                    415, arrayList.get(0),
                    445, arrayList.get(1),
                    480, arrayList.get(2),
                    515, arrayList.get(3),
                    555, arrayList.get(4),
                    590, arrayList.get(5),
                    630, arrayList.get(6),
                    680, arrayList.get(7),
            };
            int exposureVal = (int) Math.round(arrayList.get(8));

            ;

            float val = Func(inputPnt, inputStart, inputEnd, graphs).get(program.getX() - 401);
            float a = program.getA();
            float b = program.getB();
            float c = program.getC();

            return a * val * val + b * val + c;



        } catch (Exception e) {
            e.printStackTrace();
        }

        return  -1;

    }


    ArrayList<Float> Func(double[] inputArray, int inputStart, int inputEnd, int graphs) {
        ArrayList<Float> resultData = new ArrayList<>();

        double[] p = new double[2];
        int startingIndex = (inputStart - 1) * 2;
        int endingIndex = (inputEnd - 1) * 2;
        int n = (inputEnd - inputStart);
        double interval = (double) n / graphs;

        float[] sliced = new float[n * 2 + 3];
        sliced[0] = 0;
        int j = 1;
        for (int k = startingIndex; k < endingIndex + 2; k++) {
            sliced[j] = (float) inputArray[k];
            j++;
        }

        DecimalFormat df = new DecimalFormat("#.###");

        for (double t = 0.0; t <= n; t += interval) {
            getPoint(p, t, sliced, n * 2 + 2);
            float num = Float.parseFloat(df.format(p[1]));
            resultData.add(num);
        }
        return resultData;
    }

    void getPoint(double[] p, double t, float[] points, int noOfPoints)   // t = <0,n-1>
    {
        float[] pnt = new float[noOfPoints];
        int j = 0;

        for (int i = 1; i <= noOfPoints; i++) {
            pnt[j++] = points[i];
        }
        int n = noOfPoints / 2;
        int n2 = n + n;
        int i, ii;
        float a0, a1, a2, a3, d1, d2, tt, ttt;
        float[] p0, p1, p2, p3;
        // handle t out of range
        if (t <= 0.0f) {
            p[0] = pnt[0];
            p[1] = pnt[1];
            return;
        }
        if (t >= n - 1) {
            p[0] = pnt[n2 - 2];
            p[1] = pnt[n2 - 1];
            return;
        }
        // select patch
        i = (int) Math.floor(t);             // start point of patch
        t -= i;                   // parameter <0,1>
        i <<= 1;
        tt = (float) (t * t);
        ttt = (float) (tt * t);
        // control points
        ii = i - 2;
        if (ii < 0) ii = 0;
        if (ii >= n2) ii = n2 - 2;
        p0 = copy(pnt, ii);
        ii = i;
        if (ii < 0) ii = 0;
        if (ii >= n2) ii = n2 - 2;
        p1 = copy(pnt, ii);
        ii = i + 2;
        if (ii < 0) ii = 0;
        if (ii >= n2) ii = n2 - 2;
        p2 = copy(pnt, ii);
        ii = i + 4;
        if (ii < 0) ii = 0;
        if (ii >= n2) ii = n2 - 2;
        p3 = copy(pnt, ii);
        // loop all dimensions
        for (i = 0; i < 2; i++) {
            // compute polynomial coefficients
            d1 = (float) (0.5 * (p2[i] - p0[i]));
            d2 = (float) (0.5 * (p3[i] - p1[i]));
            a0 = p1[i];
            a1 = d1;
            a2 = (float) ((3.0 * (p2[i] - p1[i])) - (2.0 * d1) - d2);
            a3 = (float) (d1 + d2 + (2.0 * (-p2[i] + p1[i])));
            // compute point coordinate
            p[i] = a0 + (a1 * t) + (a2 * tt) + (a3 * ttt);
        }
    }

    private float[] copy(float[] pnt, int i) {
        List<Float> list = new ArrayList<>();
        for (int j = i; j < pnt.length; j++) {
            list.add(pnt[j]);
        }

        float[] p = new float[list.size()];
        int in = 0;
        for (Float value : list)
            p[in++] = value;

        return p;
    }

    public void sendMessage(String signal ,Program selectedProgram) {
            Log.i(TAG, signal);

            if (mmSocket != null) {
                Thread calculateThread = new Thread(() -> {
                    try {
                        mmSocket.getOutputStream().write(signal.getBytes());
                        getMessage(selectedProgram);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                calculateThread.start();
            }
    }

    public void getMessage(Program selectedProgram) {
        ArrayList<Double> arrayList = new ArrayList<>();
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            InputStream mmInputStream = mmSocket.getInputStream();
            while (true) {
                try {
                    bytes = mmInputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    String[] separated = readMessage.split("/");
                    for (String s : separated) {
                        Log.e("readMessage", s);
                        try {
                            arrayList.add(Double.parseDouble(s));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (arrayList.size() > 7) {
                        processValues(arrayList, selectedProgram);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectProgram(Program program) {
        selectedProgram = program;


    }
}
