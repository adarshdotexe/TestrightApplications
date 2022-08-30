package com.amupys.testright2;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amupys.testright2.fragments.ConcFragment;
import com.amupys.testright2.fragments.DetailsFragment;
import com.amupys.testright2.fragments.SpectraFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    private static final int REQUEST_CODE = 111;
    public static final int MAX_EXPOSURE = 40000;
    public static final String INVALID_EXPOSURE_VALUE = "Invalid exposure value";
    private static final String TAG = "LED";
    Button btnDis, btnConnect, btnBlank, btnAbs, btnIntensity, btnSave, btnAuto;
    EditText edtTime;
    int exposureVal;
    RelativeLayout relConnect;
    TextView shareLogs;
    String address = null;
    private ProgressDialog progress, progressData;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    public static boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ArrayList<Float> valuesListAbs, valuesListIntensity, valuesListBlank;
    DetailsFragment detailsFragment;
    SpectraFragment spectraFragment;
    ConcFragment concFragment;
    boolean show, forT;
    int mode; // 1 for blank, 2 for abs, 3 for intensity, 4 for auto
    private String[] permissions;
    List<String> listPermissionsNeeded = new ArrayList<>();
    public static int[] wavelengths = new int[6];
    SharedPreferences sharedPreferences;
    private Set<BluetoothDevice> pairedDevices;
    Handler handler;
    Runnable runnable;
    Thread calculateThread;
    ArrayList<Double> arrayList = new ArrayList<>();
    public static ArrayList<ProgramModel> programs = new ArrayList<>();
    RadioGroup radioGroup;
    private boolean isExposureValid = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_led_control);

        btnDis = findViewById(R.id.button4);
        btnSave = findViewById(R.id.btn_save);
        btnConnect = findViewById(R.id.btn_connect);
        relConnect = findViewById(R.id.rel_connect);
        btnBlank = findViewById(R.id.btn_blank);
        btnAbs = findViewById(R.id.btn_abs);
        btnIntensity = findViewById(R.id.btn_intensity);
        btnAuto = findViewById(R.id.btn_auto);
        edtTime = findViewById(R.id.edt_time);
        radioGroup = findViewById(R.id.radio_group);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            lower than 10
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

        loadWavelengths();

        if(forT)
            radioGroup.check(R.id.radio_t);
        else
            radioGroup.check(R.id.radio_r);

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            mode = 5;
            if(i == R.id.radio_t){
                forT = true;
                sendSignal("t");
//                Log.e("onScanButtonClicked", "t");
            }else {
                forT = false;
                sendSignal("r");
//                Log.e("onScanButtonClicked", "r");
            }
        });

        btnIntensity.setOnClickListener(view -> {
            if(isExposureValid){
                mode = 3;
                sendSignal("intensity " + exposureVal);
            }else
                Toast.makeText(this, INVALID_EXPOSURE_VALUE, Toast.LENGTH_SHORT).show();
        });

        edtTime.setText(String.valueOf(exposureVal));

        edtTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    try {
                        int val = Integer.parseInt(charSequence.toString());
                        if (val >= 1 && val <= MAX_EXPOSURE) {
                            exposureVal = val;
                            isExposureValid = true;
                        } else {
                            isExposureValid = false;
                            edtTime.setError(getString(R.string.msg_range));
                        }
                    } catch (Exception e) {
                        isExposureValid = false;
                        e.printStackTrace();
                        edtTime.setError(getString(R.string.msg_range));
                    }
                } else {
                    isExposureValid = false;
                    edtTime.setError("Required");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                edtTime.requestFocus();
            }
        });

        handler = new Handler();
        runnable = () -> {
            msg("Timeout.");
            progressData.dismiss();
        };

        requestPermission();

        btnAbs.setOnClickListener(view -> {
            mode = 2;
            sendSignal("abs");
        });

        btnBlank.setOnClickListener(view -> {
            mode = 1;
            sendSignal("blank");
        });

        btnConnect.setOnClickListener(view -> {
            Intent intent = new Intent(ledControl.this, DeviceList.class);
            startActivityForResult(intent, 11); // suppose requestCode == 2
        });

        btnDis.setOnClickListener(v -> Disconnect());

        btnSave.setOnClickListener(view -> {
            saveFile();
        });

        btnAuto.setOnClickListener(view -> {
            mode = 4;
            sendSignal("auto");
        });

        detailsFragment = new DetailsFragment();
        spectraFragment = new SpectraFragment();
        concFragment = new ConcFragment();

        ViewPager2 viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        viewPager.setAdapter(new ViewPagerAdapter(this));
        viewPager.setUserInputEnabled(false);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("OD");
                    else if (position == 1)tab.setText("Spectra");
                    else tab.setText("Conc.");
                }).attach();

        shareLogs = findViewById(R.id.share_logs);
//        shareLogs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try{
//                    File logFile = generateLog();
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
//                    intent.setType("multipart/");
//                    startActivity(intent);
//                }catch (Exception e){
//                    Log.e(TAG, e.getMessage());
////                    msg(e.getMessage());
//                }
//            }
//        });

//        for testing
//        inputPnt = new double[]{
//                415, 134,
//                445, 440,
//                480, 388,
//                515, 600,
//                555, 890,
//                590, 944,
//                630, 923,
//                680, 825,
//        };
//        forAbs = true;
//        valuesListAbs = new ArrayList<>();
//        Func(inputPnt, inputStart, inputEnd, graphs);
//        spectraFragment.receiveData(valuesListAbs);
//        saveFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //Indicates the local Bluetooth adapter is off.
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on. However local clients should wait for STATE_ON before attempting to use the adapter.
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful
                        // disconnection of any remote links.
                        msg("Bluetooth disabled or device disconnected");
                        isBtConnected = false;
                        relConnect.setVisibility(View.VISIBLE);

                        btnBlank.setEnabled(isBtConnected);
                        btnAbs.setEnabled(isBtConnected);
                        btnIntensity.setEnabled(isBtConnected);
                        btnDis.setEnabled(isBtConnected);
                        btnAuto.setEnabled(isBtConnected);
                        break;
                }
            }
        }
    };

    private void loadWavelengths() {
        sharedPreferences = getSharedPreferences("testRight", MODE_PRIVATE);
        for (int i = 0; i < 6; i++) {
            String key = String.valueOf(i);
            wavelengths[i] = sharedPreferences.getInt(key, 0);
        }

        forT = sharedPreferences.getBoolean("forT", true);
        exposureVal = sharedPreferences.getInt("exposureVal", 2000);

        if (wavelengths[0] == 0) {
            wavelengths = new int[]{420, 480, 520, 580, 620, 680};
        }
    }

    private void saveWavelength() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 0; i < 6; i++) {
            String key = String.valueOf(i);
            editor.putInt(key, wavelengths[i]);
        }

        editor.putBoolean("forT", forT);
        editor.putInt("exposureVal", exposureVal);

        editor.apply();
    }

    private void saveFile() {
        StringBuilder builder = new StringBuilder();
        int j = 0;
        ArrayList<Float> temp = null;

        if (mode == 2) {
            temp = valuesListAbs;
            builder.append("wavelength, abs");
        } else if (mode == 3) {
            builder.append("wavelength, intensity");
            temp = valuesListIntensity;
        } else if (mode == 1){
            builder.append("wavelength, blank");
            temp = valuesListBlank;
        }
        if(temp != null) {
            for (int i = 401; i <= 700; i++) {
                if (temp.size() > j) {
                    builder.append("\n").append(i).append(",").append(temp.get(j++));
                }
            }
        }

//        Log.e("Builder", builder.toString());
        //writing the data to a CSV file
        try {
            //exporting
            File directoryDownload;
            File logDir;
            boolean create;

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                logDir = new File(this.getExternalFilesDir("Testright"), "Testright");
            } else {
                directoryDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                logDir = new File(directoryDownload.getAbsolutePath(), "Testright"); //Creates a new folder in DOWNLOAD directory
            }
            if (!logDir.exists()) create = logDir.mkdirs();
            else create = true;

            if (create) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ledControl.this);
                alert.setTitle("Save as");

                final EditText editText = new EditText(ledControl.this);
                editText.setText(String.valueOf(System.currentTimeMillis()));
                alert.setView(editText);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

                alert.setPositiveButton("Save", (dialogInterface, i) -> {
//                        Log.e("path", file.getAbsolutePath());
                    String name = editText.getText().toString();
                    if(!name.isEmpty()){
                        File file = new File(logDir, name + "_data.csv");
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(file);
                            out.write(builder.toString().getBytes()); //Write the obtained string to csv
                            out.close();
                            msg("Entry saved to " + logDir.getPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                alert.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });
                alert.show();
            } else
                Toast.makeText(this, "Directory create failed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            msg(e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11 && resultCode == RESULT_OK && data != null) {
            address = data.getStringExtra(DeviceList.EXTRA_ADDRESS);

            new ConnectBT().execute();
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                fillList();
            } catch (Exception e) {
                msg(e.getMessage());
            }
        }
    }

    @Override
    public void onBackPressed() {
        saveWavelength();
        if (isBtConnected)
            Toast.makeText(this, "Tap the disconnect button to exit", Toast.LENGTH_SHORT).show();
        else
            super.onBackPressed();
    }

    @SuppressLint("MissingPermission")
    private void autoConnect() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        } else {
            fillList();
        }
    }

    @SuppressLint("MissingPermission")
    private void fillList() {
        boolean flag = false;
        pairedDevices = myBluetooth.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().contains("HC-05")) {
                    address = bt.getAddress();
                    flag = true;
                    new ConnectBT().execute();
                    break;
                }
            }
            if (!flag) {
//                not found
                msg("HC-05 Bluetooth device not found.");
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
    }

    private void requestPermission() {
        int result;
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(ledControl.this, listPermissionsNeeded.toArray(new
                            String[listPermissionsNeeded.size()]),
                    REQUEST_CODE);
        } else {
            autoConnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                autoConnect();
            } else {
                showSettingsDialog();
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ledControl.this);

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

    private void sendSignal(String signal) {
        Log.i(TAG, signal);

        shareLogs.setText("");
        if (btSocket != null) {
            calculateThread = new Thread(() -> {
                try {
                    btSocket.getOutputStream().write(signal.getBytes());
                    if(mode != 5) getMessage();
//                        msg("data send");
                } catch (IOException e) {
                    e.printStackTrace();
                    msg(e.getMessage());
                }
            });
            calculateThread.start();
        }
    }

    public void getMessage() {
        arrayList.clear();
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            InputStream mmInputStream = btSocket.getInputStream();
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
                        if (arrayList.size() > 7)
                            processValues(arrayList);

                } catch (Exception e) {
                    progressData.dismiss();
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processValues(ArrayList<Double> arrayList) {
//        StringBuilder builder = new StringBuilder();
        try {
            inputPnt = new double[]{
                    415, arrayList.get(0),
                    445, arrayList.get(1),
                    480, arrayList.get(2),
                    515, arrayList.get(3),
                    555, arrayList.get(4),
                    590, arrayList.get(5),
                    630, arrayList.get(6),
                    680, arrayList.get(7),
            };
            exposureVal = (int)Math.round(arrayList.get(8));

            Func(inputPnt, inputStart, inputEnd, graphs);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
//                                    Ending the receiving loop and stopping animation
        if (mode == 2) {
            try {
                showToastOnlyOnce("Abs is taken");
                detailsFragment.setData(valuesListAbs);
                detailsFragment.onButtonClick();
            } catch (Exception e) {
                e.printStackTrace();
                msg(e.getMessage());
            }

            ledControl.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        concFragment.setValuesList(valuesListAbs);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            spectraFragment.receiveData(valuesListAbs);
        } else if (mode == 3) {
            try {
                showToastOnlyOnce("Intensity is taken");
                detailsFragment.setData(valuesListIntensity);
                detailsFragment.onButtonClick();
            } catch (Exception e) {
                e.printStackTrace();
                msg(e.getMessage());
            }

            spectraFragment.receiveData(valuesListIntensity);
        } else if(mode == 1) {
            showToastOnlyOnce("Blank successful");
            spectraFragment.receiveData(valuesListBlank);
//                                        if(valuesListBlank.get(0) == 0)
//                                            msg("Blank received 0");
//                                        else
//                                            msg("Blank received 1");
        }

        ledControl.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(runnable);

                edtTime.setText(String.valueOf(exposureVal));
                edtTime.clearFocus();

                progressData.dismiss();
                btnSave.setVisibility(View.VISIBLE);
            }
        });
    }

    String state = "";
    int n = 8;    // points
    int n2 = n + n;
    double[] inputPnt;    // points x,y ..

    //--------------Input Variables----------------
    int inputStart = 1;
    int inputEnd = 8;
    int graphs = 300;
    //---------------------------------------------


    void Func(double[] inputArray, int inputStart, int inputEnd, int graphs) {
        double[] p = new double[2];
        double t;
        int startingIndex = (inputStart - 1) * 2;
        int endingIndex = (inputEnd - 1) * 2;
        int n = (inputEnd - inputStart);
        double interval = (double) n / graphs;
        int i = 0;

        float[] sliced = new float[n * 2 + 3];
        sliced[0] = 0;
        int j = 1;
        for (int k = startingIndex; k < endingIndex + 2; k++) {
            sliced[j] = (float) inputArray[k];
            j++;
        }

        // for(int a=0;a<n * 2 + 2;a++){
        //   Serial.print(sliced[a]);
        //   Serial.print(" , ");
        // }

        DecimalFormat df;
        if(mode == 3)
            df = new DecimalFormat("#");
        else
            df = new DecimalFormat("#.###");

        float num;

        for (t = 0.0; t <= n; t += interval) {
            getpnt(p, t, sliced, n * 2 + 2);
            i++;

            num = Float.parseFloat(df.format(p[1]));

            try {
//                Log.e("DATA", String.valueOf(num));
                if (mode == 2) {
                    valuesListAbs.add(num);
                } else if (mode == 3) {
                    valuesListIntensity.add(num);
                } else {
//                  for blank
                    valuesListBlank.add(num);
                }
//              Log.e("DATA", data);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            Serial.println(p[1]);
        }
    }

    void getpnt(double[] p, double t, float[] pont, int noOfPnts)   // t = <0,n-1>
    {
        float[] pnt = new float[noOfPnts];
        int j = 0;

        for (int i = 1; i <= noOfPnts; i++) {
            pnt[j++] = pont[i];
        }
        int n = noOfPnts / 2;
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
            // compute polynomial coeficients
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

    private void showToastOnlyOnce(String text) {
        if (show) {
            msg(text);
            show = false;
        }
    }

    private File generateLog() {
        File logFolder = new File(Environment.getExternalStorageDirectory(), "Amupys");
        if (!logFolder.exists()) {
            logFolder.mkdir();
        }
        String filename = "myapp_log_" + new Date().getTime() + ".log";

        File logFile = new File(logFolder, filename);

        try {
            String[] cmd = new String[]{"logcat", "-f", logFile.getAbsolutePath(), "-v", "time", "ActivityManager:W", "myapp:D"};
            Runtime.getRuntime().exec(cmd);
            msg("Log generated to: " + filename);
            return logFile;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

        return null;
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
                msg("Bluetooth device disconnected successfully");
                isBtConnected = false;
                saveWavelength();

                finish();
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return detailsFragment;
            } else if (position == 1){
                return spectraFragment;
            }else
                return concFragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private void msg(String s) {
        ledControl.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
            } else {
                msg("Connected");
                isBtConnected = true;
                relConnect.setVisibility(View.GONE);

                btnBlank.setEnabled(true);
                btnAbs.setEnabled(true);
                btnIntensity.setEnabled(true);
                btnDis.setEnabled(true);
                btnAuto.setEnabled(true);

                mode = 5;
                if(forT){
                    sendSignal("t");
//                Log.e("onScanButtonClicked", "t");
                }else {
                    sendSignal("r");
//                Log.e("onScanButtonClicked", "r");
                }
            }

            progress.dismiss();
        }
    }
}
