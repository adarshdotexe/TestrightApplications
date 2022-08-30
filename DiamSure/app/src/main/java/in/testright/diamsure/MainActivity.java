package in.testright.diamsure;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    public static final String DIFF_VALUE_FOR_SELECTION = "DiffValueForSelection";
    private static final String TAG = "DIAMSURE";
    public static int count = 0;
    private final String[] permissions = {"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.MANAGE_EXTERNAL_STORAGE"};
    int DiffValueForSelection_blue;
    int DiffValueForSelection_green;
    int DiffValueForSelection_red;
    PixelColor PixelMax = new PixelColor(0, 0, 0);
    PixelColor PixelMin = new PixelColor(0, 0, 0);
    PixelColor PixelPhosMax = new PixelColor(0, 0, 0);
    PixelColor PixelPhosMin = new PixelColor(0, 0, 0);
    Bitmap bitmap1;
    Bitmap bitmap2;
    Bitmap bitmap3;
    Bitmap bitmap1blur;
    Bitmap bitmap2blur;
    Bitmap bitmap3blur;
    Button btn_snap;
    ArrayList<PixelColor> colorArrayList = new ArrayList<>();
    ArrayList<PixelColor> colorArrayListPhos = new ArrayList<>();
    Uri imageUri1;
    Uri imageUri2;
    Uri imageUri3;
    ActivityResultLauncher<Intent> launcher;
    TextView mainTxt;
    LinearLayout main_card;
    ImageView main_image;
    ProgressBar main_progress;
    ImageView more_menu;
    ArrayList<PixelColor> newColorArray = new ArrayList<>();
    Paint paint;
    Paint paintPhos;
    ImageView phos_menu;
    SharedPreferences preferences;
    TextView resInfo;
    ImageView sub1;
    ImageView sub2;
    TextView txtUpdate;
    private CameraFragment cameraFragment;
    private boolean isBlueChecked;
    private boolean isBluePhosChecked;
    private boolean isGreenChecked;
    private boolean isGreenPhosChecked;
    private boolean isRedChecked;
    private boolean isRedPhosChecked;
    private boolean isSelectionIncreasing_blue;
    private boolean isSelectionIncreasing_green;
    private boolean isSelectionIncreasing_red;
    private Thread worker;

    public static Bitmap filter(Bitmap bitmap1, Bitmap bitmap2) {
        Pixel dark_high = new Pixel(Color.parseColor("#333333"));
        Pixel dark_low = new Pixel(Color.parseColor("#000000"));
        Pixel filter_color = new Pixel(Color.parseColor("#EE0A0A"));
        float filter_weight = 0.4f;

        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();
        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int i = width * height;
        int[] pixels = new int[i];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int blue1 = Color.blue(bitmap1.getPixel(x,y));
                int red = Color.red(bitmap2.getPixel(x,y));
                int green = Color.green(bitmap2.getPixel(x,y));
                int blue = Color.blue(bitmap2.getPixel(x,y));
                if (dark_low.b > blue1 || blue1 > dark_high.b) {
                    pixels[x+y] = Color.argb(255, (int) ((((float) filter_color.r) * (1.0f - filter_weight)) + (((float) red) * filter_weight)), (int) ((((float) filter_color.g) * (1.0f - filter_weight)) + (((float) green) * filter_weight)), (int) ((((float) filter_color.b) * (1.0f - filter_weight)) + (((float) blue) * filter_weight)));
                } else {
                    pixels[x+y] = bitmap2.getPixel(x, y);
                }
            }
        }
        createBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return createBitmap;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btn_snap = findViewById(R.id.btn_main);
        this.main_progress = findViewById(R.id.main_progress);
        this.mainTxt = findViewById(R.id.main_txt);
        this.txtUpdate = findViewById(R.id.txt_update);
        this.main_card = findViewById(R.id.main_card);
        this.main_image = findViewById(R.id.img_main);
        this.sub1 = findViewById(R.id.img_main_sub1);
        this.sub2 = findViewById(R.id.img_main_sub2);
        this.more_menu = findViewById(R.id.main_more);
        this.phos_menu = findViewById(R.id.phos_more);
        this.resInfo = findViewById(R.id.txt_main_res_info);
        btn_snap.setOnClickListener(view -> inflateFragment());
        ImageView clear = findViewById(R.id.btn_main_cancel);
        clear.setOnClickListener(view -> {
            if (worker != null) {
                worker.interrupt();
                Log.e("thread", "stopped");
            }
            clearHistory();
        });
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == -1) {
                Intent data = result.getData();
                if (!(data == null || data.getData() == null)) {
                    int i = count;
                    if (i == 0) {
                        this.imageUri1 = data.getData();
                    } else if (i == 1) {
                        this.imageUri2 = data.getData();
                    } else if (i == 2) {
                        this.imageUri3 = data.getData();
                    }
                }
                this.cameraFragment.setProceed(count);
                count++;
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("testright", 0);
        preferences = sharedPreferences;
        PixelMax.blue = sharedPreferences.getInt("blue_max", 230);
        PixelMin.blue = this.preferences.getInt("blue_min", 130);
        PixelMax.red = this.preferences.getInt("red_max", 42);
        PixelMin.red = this.preferences.getInt("red_min", 12);
        PixelMax.green = this.preferences.getInt("green_max", 67);
        PixelMin.green = this.preferences.getInt("green_min", 28);
        PixelPhosMax.blue = this.preferences.getInt("bluephos_max", 230);
        PixelPhosMin.blue = this.preferences.getInt("bluephos_min", 130);
        PixelPhosMax.red = this.preferences.getInt("redphos_max", 42);
        PixelPhosMin.red = this.preferences.getInt("redphos_min", 12);
        PixelPhosMax.green = this.preferences.getInt("greenphos_max", 67);
        PixelPhosMin.green = this.preferences.getInt("greenphos_min", 28);
        isBlueChecked = this.preferences.getBoolean("isBlueChecked", true);
        isRedChecked = this.preferences.getBoolean("isRedChecked", false);
        isGreenChecked = this.preferences.getBoolean("isGreenChecked", false);
        isBluePhosChecked = this.preferences.getBoolean("isBluePhosChecked", true);
        isRedPhosChecked = this.preferences.getBoolean("isRedPhosChecked", false);
        isGreenPhosChecked = this.preferences.getBoolean("isGreenPhosChecked", false);
        isSelectionIncreasing_blue = this.preferences.getBoolean("isSelectionIncreasing_blue", true);
        isSelectionIncreasing_red = this.preferences.getBoolean("isSelectionIncreasing_red", true);
        isSelectionIncreasing_green = this.preferences.getBoolean("isSelectionIncreasing_green", true);
        DiffValueForSelection_blue = this.preferences.getInt(DIFF_VALUE_FOR_SELECTION, 10);
        DiffValueForSelection_red = this.preferences.getInt("DiffValueForSelection_red", 10);
        DiffValueForSelection_green = this.preferences.getInt("DiffValueForSelection_green", 10);
        more_menu.setOnClickListener(view -> {
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.dialog_range, null);
            TextInputEditText etMaxBlue = alertLayout.findViewById(R.id.tiet_max);
            TextInputEditText etMinBlue = alertLayout.findViewById(R.id.tiet_min);
            TextInputEditText etMaxRed = alertLayout.findViewById(R.id.max_val_red);
            TextInputEditText etMinRed = alertLayout.findViewById(R.id.min_val_red);
            TextInputEditText etMaxGreen = alertLayout.findViewById(R.id.max_val_green);
            TextInputEditText etMinGreen = alertLayout.findViewById(R.id.min_val_green);
            RadioGroup radioGroup_blue = alertLayout.findViewById(R.id.radio_group_blue);
            RadioGroup radioGroup_red = alertLayout.findViewById(R.id.radio_group_red);
            RadioGroup radioGroup_green = alertLayout.findViewById(R.id.radio_group_green);
            TextInputEditText diff_val_blue = alertLayout.findViewById(R.id.tiet_difference_blue);
            TextInputEditText diff_val_red = alertLayout.findViewById(R.id.tiet_difference_red);
            TextInputEditText diff_val_green = alertLayout.findViewById(R.id.tiet_difference_green);
            CheckBox checkBoxBlue = alertLayout.findViewById(R.id.check_blue);
            CheckBox checkBoxRed = alertLayout.findViewById(R.id.check_red);
            CheckBox checkBoxGreen = alertLayout.findViewById(R.id.check_green);
            if (isSelectionIncreasing_blue) {
                radioGroup_blue.check(R.id.radio_increasing_blue);
            } else {
                radioGroup_blue.check(R.id.radio_decreasing_blue);
            }
            if (isSelectionIncreasing_red) {
                radioGroup_red.check(R.id.radio_increasing_red);
            } else {
                radioGroup_red.check(R.id.radio_decreasing_red);
            }
            if (isSelectionIncreasing_green) {
                radioGroup_green.check(R.id.radio_increasing_green);
            } else {
                radioGroup_green.check(R.id.radio_decreasing_green);
            }
            etMaxBlue.setText(String.valueOf(PixelMax.blue));
            etMinBlue.setText(String.valueOf(PixelMin.blue));
            etMaxRed.setText(String.valueOf(PixelMax.red));
            etMinRed.setText(String.valueOf(PixelMin.red));
            etMaxGreen.setText(String.valueOf(PixelMax.green));
            etMinGreen.setText(String.valueOf(PixelMin.green));
            diff_val_blue.setText(String.valueOf(DiffValueForSelection_blue));
            diff_val_red.setText(String.valueOf(DiffValueForSelection_red));
            diff_val_green.setText(String.valueOf(DiffValueForSelection_green));
            checkBoxBlue.setChecked(isBlueChecked);
            checkBoxRed.setChecked(isRedChecked);
            checkBoxGreen.setChecked(isGreenChecked);
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Set Range");
            alert.setView(alertLayout);
            alert.setCancelable(false);
            alert.setNegativeButton("Cancel", null);
            alert.setPositiveButton("Done", (dialogInterface, i) -> {
                PixelMax.blue = Integer.parseInt(Objects.requireNonNull(etMaxBlue.getText()).toString());
                PixelMin.blue = Integer.parseInt(Objects.requireNonNull(etMinBlue.getText()).toString());
                PixelMax.red = Integer.parseInt(Objects.requireNonNull(etMaxRed.getText()).toString());
                PixelMin.red = Integer.parseInt(Objects.requireNonNull(etMinRed.getText()).toString());
                PixelMax.green = Integer.parseInt(Objects.requireNonNull(etMaxGreen.getText()).toString());
                PixelMin.green = Integer.parseInt(Objects.requireNonNull(etMinGreen.getText()).toString());
                isRedChecked = checkBoxRed.isChecked();
                isBlueChecked = checkBoxBlue.isChecked();
                isGreenChecked = checkBoxGreen.isChecked();
                DiffValueForSelection_blue = Integer.parseInt(Objects.requireNonNull(diff_val_blue.getText()).toString());
                DiffValueForSelection_red = Integer.parseInt(Objects.requireNonNull(diff_val_red.getText()).toString());
                DiffValueForSelection_green = Integer.parseInt(Objects.requireNonNull(diff_val_green.getText()).toString());
                boolean z = true;
                isSelectionIncreasing_blue = radioGroup_blue.getCheckedRadioButtonId() == R.id.radio_increasing_blue;
                isSelectionIncreasing_red = radioGroup_red.getCheckedRadioButtonId() == R.id.radio_increasing_red;
                if (radioGroup_green.getCheckedRadioButtonId() != R.id.radio_increasing_green) {
                    z = false;
                }
                isSelectionIncreasing_green = z;
                saveRange();
            });
            alert.create().show();
        });
        this.phos_menu.setOnClickListener(view -> {
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.dialog_range_phos, null);
            TextInputEditText etMaxBlue = alertLayout.findViewById(R.id.max_blue_phos);
            TextInputEditText etMinBlue = alertLayout.findViewById(R.id.min_blue_phos);
            TextInputEditText etMaxRed = alertLayout.findViewById(R.id.max_red_phos);
            TextInputEditText etMinRed = alertLayout.findViewById(R.id.min_red_phos);
            TextInputEditText etMaxGreen = alertLayout.findViewById(R.id.max_green_phos);
            TextInputEditText etMinGreen = alertLayout.findViewById(R.id.min_green_phos);
            CheckBox checkBoxRed = alertLayout.findViewById(R.id.check_red_phos);
            CheckBox checkBoxBlue = alertLayout.findViewById(R.id.check_blue_phos);
            CheckBox checkBoxGreen = alertLayout.findViewById(R.id.check_green_phos);
            etMaxBlue.setText(String.valueOf(PixelPhosMax.blue));
            etMinBlue.setText(String.valueOf(PixelPhosMin.blue));
            etMaxRed.setText(String.valueOf(PixelPhosMax.red));
            etMinRed.setText(String.valueOf(PixelPhosMin.red));
            etMaxGreen.setText(String.valueOf(PixelPhosMax.green));
            etMinGreen.setText(String.valueOf(PixelPhosMin.green));
            checkBoxBlue.setChecked(isBluePhosChecked);
            checkBoxRed.setChecked(isRedPhosChecked);
            checkBoxGreen.setChecked(isGreenPhosChecked);
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Set Range");
            alert.setView(alertLayout);
            alert.setCancelable(false);
            alert.setNegativeButton("Cancel", null);
            alert.setPositiveButton("Done", (dialogInterface, i) -> {
                PixelPhosMax.blue = Integer.parseInt(Objects.requireNonNull(etMaxBlue.getText()).toString());
                PixelPhosMin.blue = Integer.parseInt(Objects.requireNonNull(etMinBlue.getText()).toString());
                PixelPhosMax.red = Integer.parseInt(Objects.requireNonNull(etMaxRed.getText()).toString());
                PixelPhosMin.red = Integer.parseInt(Objects.requireNonNull(etMinRed.getText()).toString());
                PixelPhosMax.green = Integer.parseInt(Objects.requireNonNull(etMaxGreen.getText()).toString());
                PixelPhosMin.green = Integer.parseInt(Objects.requireNonNull(etMinGreen.getText()).toString());
                isRedPhosChecked = checkBoxRed.isChecked();
                isBluePhosChecked = checkBoxBlue.isChecked();
                isGreenPhosChecked = checkBoxGreen.isChecked();
                saveRange();
            });
            alert.create().show();
        });
        Paint paint2 = new Paint();
        this.paint = paint2;
        paint2.setStyle(Paint.Style.FILL);
        this.paint.setColor(ContextCompat.getColor(this, R.color.marker));
        Paint paint3 = new Paint();
        this.paintPhos = paint3;
        paint3.setStyle(Paint.Style.FILL);
        this.paintPhos.setColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("myTag") != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.remove(Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag("myTag"))).commit();
        } else {
            if (main_card.getVisibility() != View.VISIBLE) {
                super.onBackPressed();
            }
        }
    }

    private void saveRange() {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt("blue_max", this.PixelMax.blue);
        editor.putInt("blue_min", this.PixelMin.blue);
        editor.putInt("red_max", this.PixelMax.red);
        editor.putInt("red_min", this.PixelMin.red);
        editor.putInt("green_max", this.PixelMax.green);
        editor.putInt("green_min", this.PixelMin.green);
        editor.putInt("bluephos_max", this.PixelPhosMax.blue);
        editor.putInt("bluephos_min", this.PixelPhosMin.blue);
        editor.putInt("redphos_max", this.PixelPhosMax.red);
        editor.putInt("redphos_min", this.PixelPhosMin.red);
        editor.putInt("greenphos_max", this.PixelPhosMax.green);
        editor.putInt("greenphos_min", this.PixelPhosMin.green);
        editor.putInt(DIFF_VALUE_FOR_SELECTION, this.DiffValueForSelection_blue);
        editor.putInt("DiffValueForSelection_red", this.DiffValueForSelection_red);
        editor.putInt("DiffValueForSelection_green", this.DiffValueForSelection_green);
        editor.putBoolean("isBlueChecked", this.isBlueChecked);
        editor.putBoolean("isRedChecked", this.isRedChecked);
        editor.putBoolean("isGreenChecked", this.isGreenChecked);
        editor.putBoolean("isBluePhosChecked", this.isBluePhosChecked);
        editor.putBoolean("isRedPhosChecked", this.isRedPhosChecked);
        editor.putBoolean("isGreenPhosChecked", this.isGreenPhosChecked);
        editor.putBoolean("isSelectionIncreasing_blue", this.isSelectionIncreasing_blue);
        editor.putBoolean("isSelectionIncreasing_red", this.isSelectionIncreasing_red);
        editor.putBoolean("isSelectionIncreasing_green", this.isSelectionIncreasing_green);
        editor.apply();
    }

    private void clearHistory() {
        count = 0;
        this.imageUri1 = null;
        this.imageUri2 = null;
        this.imageUri3 = null;
        this.bitmap1 = null;
        this.bitmap2 = null;
        this.colorArrayList.clear();
        this.newColorArray.clear();
        this.main_card.setVisibility(View.GONE);
        this.more_menu.setVisibility(View.VISIBLE);
    }

    private void requestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : this.permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != 0) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 111);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions2, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions2, grantResults);
        if (requestCode != 111) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCamButtonClick() {
        requestPermissions();
        Intent intent = new Intent(this, CameraActivity.class);
        intent.setType("image/*");
        intent.putExtra("Uri", getImageUri());
        intent.setAction("android.intent.action.GET_CONTENT");
        this.launcher.launch(intent);
    }

    public void onGalleryButtonClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        this.launcher.launch(intent);
    }

    public void onProceedClick() {
        onBackPressed();
        this.main_card.setVisibility(View.VISIBLE);
        this.more_menu.setVisibility(View.GONE);
        this.main_image.setImageURI(this.imageUri2);
        this.sub1.setImageURI(this.imageUri1);
        this.sub2.setImageURI(this.imageUri2);
        this.main_progress.setVisibility(View.VISIBLE);
        this.txtUpdate.setText("Analysing..");
        this.resInfo.setText("Calculating...");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startAnalysis();
            }
        });
        this.worker = thread;
        thread.start();
    }

    /* access modifiers changed from: private */
    public void startAnalysis() {
        bitmap1 = getBitmap(this.imageUri1);
        bitmap2 = getBitmap(this.imageUri2);
        bitmap3 = getBitmap(this.imageUri3);
        bitmap1blur = getBitmap(this.imageUri1);
        bitmap2blur = getBitmap(this.imageUri2);
        bitmap3blur = getBitmap(this.imageUri3);
        colorArrayList.clear();
        colorArrayListPhos.clear();
//        if (OpenCVLoader.initDebug()) {
//            Log.d("myTag", "OpenCV loaded");
//        }
//        blur(bitmap1, bitmap1blur, 5, true);
//        blur(bitmap3, bitmap3blur, 5, false);
//        blur(bitmap3, bitmap3blur, 5, false);

        int x = 0;
        while (x < this.bitmap1.getWidth()) {
            int y = 0;
            while (y < this.bitmap1.getHeight()) {
                int pixelphos = this.bitmap3blur.getPixel(x, y);
                int pixel = this.bitmap1blur.getPixel(x, y);
                boolean toAdd = false;
                boolean toAddPhos = false;
                if (this.isBlueChecked) {
                    int blueValue = Color.blue(pixel);
                    toAdd = blueValue > this.PixelMin.blue && blueValue < this.PixelMax.blue;
                }
                if (this.isRedChecked && (!this.isBlueChecked || toAdd)) {
                    int redValue = Color.red(pixel);
                    toAdd = redValue > this.PixelMin.red && redValue < this.PixelMax.red;
                }
                if (this.isGreenChecked && ((!this.isBlueChecked || toAdd) && (!this.isRedChecked || toAdd))) {
                    int greenValue = Color.green(pixel);
                    toAdd = greenValue > this.PixelMin.green && greenValue < this.PixelMax.green;
                }
                if (this.isBluePhosChecked) {
                    int blueValue2 = Color.blue(pixelphos);
                    toAddPhos = blueValue2 > this.PixelPhosMin.blue && blueValue2 < this.PixelPhosMax.blue;
                }
                if (this.isRedPhosChecked && (!this.isBluePhosChecked || toAddPhos)) {
                    int redValue2 = Color.red(pixelphos);
                    toAddPhos = redValue2 > this.PixelPhosMin.red && redValue2 < this.PixelPhosMax.red;
                }
                if (this.isGreenPhosChecked && ((!this.isBluePhosChecked || toAddPhos) && (!this.isRedChecked || toAddPhos))) {
                    int greenValue2 = Color.green(pixelphos);
                    toAddPhos = greenValue2 > this.PixelPhosMin.green && greenValue2 < this.PixelPhosMax.green;
                }
                if (toAdd) {
                    try {
                        pixel = this.bitmap1.getPixel(x, y);
                        PixelColor pixelColor = new PixelColor(Color.red(pixel), Color.blue(pixel), Color.green(pixel));
                        pixelColor.setX(x);
                        pixelColor.setY(y);
                        this.colorArrayList.add(pixelColor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (toAddPhos) {
                    PixelColor pixelColor2 = new PixelColor(Color.red(pixel), Color.blue(pixel), Color.green(pixel));
                    pixelColor2.setX(x);
                    pixelColor2.setY(y);
                    this.colorArrayListPhos.add(pixelColor2);
                }
                y++;
            }
            x++;
        }
        for (int i = 0; i < this.colorArrayList.size(); i++) {
            PixelColor firstPixelColor = this.colorArrayList.get(i);
            boolean toRemove = false;
            int mod = -1;
            if (this.isBlueChecked) {
                toRemove = (firstPixelColor.blue - Color.blue(this.bitmap2.getPixel(firstPixelColor.x, firstPixelColor.y))) * (this.isSelectionIncreasing_blue ? 1 : -1) < this.DiffValueForSelection_blue;
            }
            if (this.isRedChecked) {
                toRemove = (firstPixelColor.red - Color.red(this.bitmap2.getPixel(firstPixelColor.x, firstPixelColor.y))) * (this.isSelectionIncreasing_red ? 1 : -1) < this.DiffValueForSelection_red;
            }
            if (this.isGreenChecked) {
                int greenValueSecond = Color.green(this.bitmap2.getPixel(firstPixelColor.x, firstPixelColor.y));
                if (this.isSelectionIncreasing_green) {
                    mod = 1;
                }
                toRemove = (firstPixelColor.green - greenValueSecond) * mod < this.DiffValueForSelection_green;
            }
            if (!toRemove) {
                this.newColorArray.add(firstPixelColor);
            }
        }
        runOnUiThread(() -> {
            resInfo.setText(String.format("Result:\nImage size: %sx%s\nPixels marked red: %s\nPixels marked orange: %s\nPixels Compared:%s", bitmap1.getWidth(), bitmap1.getHeight(), newColorArray.size(), colorArrayListPhos.size(), colorArrayList.size()));
            if (!(newColorArray.size() == 0 && colorArrayListPhos.size() == 0)) {
                drawpoint();
            }
            main_progress.setVisibility(View.GONE);
            txtUpdate.setText("Analysis Completed");
        });
    }

//    private void blur(Bitmap bitmap, Bitmap bitmapblur, int f, Boolean open) {
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
//        if(open) {
//            Imgproc.erode(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//            Imgproc.erode(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//            Imgproc.erode(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//            Imgproc.dilate(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//            Imgproc.dilate(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//            Imgproc.dilate(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//        }
//        else {
//            Imgproc.dilate(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//            Imgproc.erode(mat, mat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(f, f)));
//        }
//        Utils.matToBitmap(mat, bitmapblur);
//    }

    private void drawpoint() {
        Bitmap tempBitmap = Bitmap.createBitmap(this.bitmap1.getWidth(), this.bitmap1.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(this.bitmap1, 0.0f, 0.0f, null);
        for (PixelColor pixelColor : this.newColorArray) {
            tempCanvas.drawPoint((float) pixelColor.x, (float) pixelColor.y, this.paint);
        }
        for (PixelColor pixelColor2 : this.colorArrayListPhos) {
            tempCanvas.drawPoint((float) pixelColor2.x, (float) pixelColor2.y, this.paintPhos);
        }
//        blur(tempBitmap, tempBitmap, 4, false);
        this.main_image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    private Bitmap getBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri), (imageDecoder, imageInfo, source) -> {
                imageDecoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                imageDecoder.isMutableRequired();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Uri getImageUri() {
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_");
        contentValues.put("mime_type", "image/png");
        contentValues.put("relative_path", Environment.DIRECTORY_DCIM + File.separator + "Testright");
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    private void inflateFragment() {
        //begin fragment
        cameraFragment = new CameraFragment();
        cameraFragment.setFragmentCallback(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.main_frame, cameraFragment, "myTag").commit();
    }

    private static class Pixel {
        final int b;
        final int g;
        final int r;

        Pixel(int i) {
            this.r = Color.red(i);
            this.g = Color.blue(i);
            this.b = Color.green(i);
        }
    }

}
