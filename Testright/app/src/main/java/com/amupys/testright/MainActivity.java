package com.amupys.testright;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
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
import android.provider.MediaStore;
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

import com.amupys.testright.fragments.CameraFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FragmentCallback {

    private static final int PICK_IMAGE_REQUEST = 112;
    public static final String DIFF_VALUE_FOR_SELECTION = "DiffValueForSelection";
    private boolean isBlueChecked, isRedChecked, isGreenChecked, isSelectionIncreasing_blue,
            isSelectionIncreasing_red, isSelectionIncreasing_green;
    TextView mainTxt, txtUpdate;
    Button btn_snap;
    ProgressBar main_progress;
    Bitmap bitmap1, bitmap2;
    Uri imageUri1, imageUri2;
    LinearLayout main_card;
    ImageView main_image, sub1, sub2, more_menu;
    int count = 0;
    private CameraFragment cameraFragment;
    private final String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    ArrayList<PixelColor> colorArrayList = new ArrayList<>(),
    newColorArray = new ArrayList<>();
    Paint paint;
    PixelColor PixelMax = new PixelColor(0, 0, 0);
    PixelColor PixelMin = new PixelColor(0, 0, 0);
    SharedPreferences preferences;
    int DiffValueForSelection_blue, DiffValueForSelection_red, DiffValueForSelection_green;
    private Thread worker;
    TextView resInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_snap = findViewById(R.id.btn_main);
        main_progress = findViewById(R.id.main_progress);
        mainTxt = findViewById(R.id.main_txt);
        txtUpdate = findViewById(R.id.txt_update);
        main_card = findViewById(R.id.main_card);
        main_image = findViewById(R.id.img_main);
        sub1 = findViewById(R.id.img_main_sub1);
        sub2 = findViewById(R.id.img_main_sub2);
        more_menu = findViewById(R.id.main_more);
        resInfo = findViewById(R.id.txt_main_res_info);

        btn_snap.setOnClickListener(view -> inflateFragment());
        ImageView clear = findViewById(R.id.btn_main_cancel);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(worker != null ){
                    worker.interrupt();
                    Log.e("thread", "stopped");
                }
                clearHistory();
            }
        });
        preferences = getSharedPreferences("testright", MODE_PRIVATE);
        PixelMax.blue = preferences.getInt("blue_max", 230);
        PixelMin.blue = preferences.getInt("blue_min", 130);
        PixelMax.red = preferences.getInt("red_max", 42);
        PixelMin.red = preferences.getInt("red_min", 12);
        PixelMax.green = preferences.getInt("green_max", 67);
        PixelMin.green = preferences.getInt("green_min", 28);
        isBlueChecked = preferences.getBoolean("isBlueChecked", true);
        isRedChecked = preferences.getBoolean("isRedChecked", false);
        isGreenChecked = preferences.getBoolean("isGreenChecked", false);
        isSelectionIncreasing_blue = preferences.getBoolean("isSelectionIncreasing_blue", true);
        isSelectionIncreasing_red = preferences.getBoolean("isSelectionIncreasing_red", true);
        isSelectionIncreasing_green = preferences.getBoolean("isSelectionIncreasing_green", true);
        DiffValueForSelection_blue = preferences.getInt(DIFF_VALUE_FOR_SELECTION, 10);
        DiffValueForSelection_red = preferences.getInt("DiffValueForSelection_red", 10);
        DiffValueForSelection_green = preferences.getInt("DiffValueForSelection_green", 10);

        more_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.dialog_range, null);
                final TextInputEditText etMaxBlue = alertLayout.findViewById(R.id.tiet_max);
                final TextInputEditText etMinBlue = alertLayout.findViewById(R.id.tiet_min);
                final TextInputEditText etMaxRed = alertLayout.findViewById(R.id.max_val_red);
                final TextInputEditText etMinRed = alertLayout.findViewById(R.id.min_val_red);
                final TextInputEditText etMaxGreen = alertLayout.findViewById(R.id.max_val_green);
                final TextInputEditText etMinGreen = alertLayout.findViewById(R.id.min_val_green);
                final RadioGroup radioGroup_blue = alertLayout.findViewById(R.id.radio_group_blue);
                final RadioGroup radioGroup_red = alertLayout.findViewById(R.id.radio_group_red);
                final RadioGroup radioGroup_green = alertLayout.findViewById(R.id.radio_group_green);
                final TextInputEditText diff_val_blue = alertLayout.findViewById(R.id.tiet_difference_blue);
                final TextInputEditText diff_val_red = alertLayout.findViewById(R.id.tiet_difference_red);
                final TextInputEditText diff_val_green = alertLayout.findViewById(R.id.tiet_difference_green);
                final CheckBox checkBoxRed = alertLayout.findViewById(R.id.check_red);
                final CheckBox checkBoxBlue = alertLayout.findViewById(R.id.check_blue);
                final CheckBox checkBoxGreen = alertLayout.findViewById(R.id.check_green);

                if (isSelectionIncreasing_blue) radioGroup_blue.check(R.id.radio_increasing_blue);
                else radioGroup_blue.check(R.id.radio_decreasing_blue);
                if (isSelectionIncreasing_red) radioGroup_red.check(R.id.radio_increasing_red);
                else radioGroup_red.check(R.id.radio_decreasing_red);
                if (isSelectionIncreasing_green) radioGroup_green.check(R.id.radio_increasing_green);
                else radioGroup_green.check(R.id.radio_decreasing_green);

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
                // this is set the view from XML inside AlertDialog
                alert.setView(alertLayout);
                // disallow cancel of AlertDialog on click of back button and outside touch
                alert.setCancelable(false);
                alert.setNegativeButton("Cancel", (dialog, which) -> {
                });

                alert.setPositiveButton("Done", (dialog, which) -> {
                    PixelMax.blue = Integer.parseInt(etMaxBlue.getText().toString());
                    PixelMin.blue = Integer.parseInt(etMinBlue.getText().toString());
                    PixelMax.red = Integer.parseInt(etMaxRed.getText().toString());
                    PixelMin.red = Integer.parseInt(etMinRed.getText().toString());
                    PixelMax.green = Integer.parseInt(etMaxGreen.getText().toString());
                    PixelMin.green = Integer.parseInt(etMinGreen.getText().toString());
                    isRedChecked = checkBoxRed.isChecked();
                    isBlueChecked = checkBoxBlue.isChecked();
                    isGreenChecked = checkBoxGreen.isChecked();
                    DiffValueForSelection_blue = Integer.parseInt(diff_val_blue.getText().toString());
                    DiffValueForSelection_red = Integer.parseInt(diff_val_red.getText().toString());
                    DiffValueForSelection_green = Integer.parseInt(diff_val_green.getText().toString());
                    if (radioGroup_blue.getCheckedRadioButtonId() == R.id.radio_increasing_blue)
                        isSelectionIncreasing_blue = true;
                    else isSelectionIncreasing_blue = false;
                    if (radioGroup_red.getCheckedRadioButtonId() == R.id.radio_increasing_red)
                        isSelectionIncreasing_red = true;
                    else isSelectionIncreasing_red = false;
                    if (radioGroup_green.getCheckedRadioButtonId() == R.id.radio_increasing_green)
                        isSelectionIncreasing_green = true;
                    else isSelectionIncreasing_green = false;
//                    Log.e("max min", etMaxBlue.getText().toString()+etMinBlue.getText().toString());
                    saveRange();
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });

        paint = new Paint();
//        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.marker));
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("myTag") != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.remove(Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag("myTag"))).commit();
        } else if (main_card.getVisibility() == View.VISIBLE) {
        } else {
            super.onBackPressed();
        }
    }

    private void saveRange() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("blue_max", PixelMax.blue);
        editor.putInt("blue_min", PixelMin.blue);
        editor.putInt("red_max", PixelMax.red);
        editor.putInt("red_min", PixelMin.red);
        editor.putInt("green_max", PixelMax.green);
        editor.putInt("green_min", PixelMin.green);
        editor.putInt(DIFF_VALUE_FOR_SELECTION, DiffValueForSelection_blue);
        editor.putInt("DiffValueForSelection_red", DiffValueForSelection_red);
        editor.putInt("DiffValueForSelection_green", DiffValueForSelection_green);
        editor.putBoolean("isBlueChecked", isBlueChecked);
        editor.putBoolean("isRedChecked", isRedChecked);
        editor.putBoolean("isGreenChecked", isGreenChecked);
        editor.putBoolean("isSelectionIncreasing_blue", isSelectionIncreasing_blue);
        editor.putBoolean("isSelectionIncreasing_red", isSelectionIncreasing_red);
        editor.putBoolean("isSelectionIncreasing_green", isSelectionIncreasing_green);
        editor.apply();
    }

    private void clearHistory() {
        count = 0;
        imageUri1 = null;
        imageUri2 = null;
        bitmap1 = null;
        bitmap2 = null;
        colorArrayList.clear();
        newColorArray.clear();
        main_card.setVisibility(View.GONE);
        more_menu.setVisibility(View.VISIBLE);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void requestPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new
                            String[listPermissionsNeeded.size()]),
                    111);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (count == 0)
                imageUri1 = data.getData();
            else
                imageUri2 = data.getData();

            cameraFragment.setProceed(count);
            count++;
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (count == 0)
                imageUri1 = data.getData();
            else
                imageUri2 = data.getData();

            cameraFragment.setProceed(count);
            count++;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCamButtonClick() {
        requestPermissions();
        Intent intent = new Intent(this, CameraActivity.class);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(new Intent(this, CameraActivity.class), REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onGalleryButtonClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onProceedClick() {
        onBackPressed();
        main_card.setVisibility(View.VISIBLE);
        more_menu.setVisibility(View.GONE);
        main_image.setImageURI(imageUri2);
        sub1.setImageURI(imageUri1);
        sub2.setImageURI(imageUri2);

        main_progress.setVisibility(View.VISIBLE);
        txtUpdate.setText("Analysing..");
        resInfo.setText("Calculating...");

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        startAnalyse();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else
                    Toast.makeText(MainActivity.this, "Action not supported", Toast.LENGTH_SHORT).show();
            }
        });
        worker.start();
    }

    private void startAnalyse() {
        bitmap1 = getBitmap(imageUri1);
        bitmap2 = getBitmap(imageUri2);

        boolean toAdd;
        for (int x = 0; x < bitmap1.getWidth(); x++) {
            for (int y = 0; y < bitmap1.getHeight(); y++) {
                int pixel = bitmap1.getPixel(x, y);
                int blueValue = Color.blue(pixel);
                int redValue = Color.red(pixel);
                int greenValue = Color.green(pixel);
                toAdd = false;
                PixelColor pixelColor = new PixelColor(redValue, blueValue, greenValue);
                pixelColor.setX(x);
                pixelColor.setY(y);

                if(isBlueChecked){
                    if (blueValue >= PixelMin.blue && blueValue <= PixelMax.blue) {
                        toAdd = true;
                    }
                }
                if(isRedChecked){
                    if(redValue >= PixelMin.red && redValue <= PixelMax.red){
                        toAdd = true;
                    }else toAdd = false;
                }
                if(isGreenChecked){
                    if(greenValue >= PixelMin.green && greenValue <= PixelMax.green){
                        toAdd = true;
                    }else toAdd = false;
                }

                try{
                    if (toAdd) {
//                    selecting and adding passed pixel to array
                        colorArrayList.add(pixelColor);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    break;
                }
            }
        }

        Log.e("initial size", String.valueOf(colorArrayList.size()));
        boolean toRemove;
        for (int i = 0; i < colorArrayList.size(); i++) {
            PixelColor firstPixelColor = colorArrayList.get(i);
            toRemove = false; // pixel will be marked

            if(isBlueChecked){
                int blueValueSecond = Color.blue(bitmap2.getPixel(firstPixelColor.x, firstPixelColor.y));
                if(isSelectionIncreasing_blue) {
                    if ((firstPixelColor.blue - blueValueSecond) < DiffValueForSelection_blue) {
                        toRemove = true; // pixel will not marked
                    }
                }
                else {
                    if ((blueValueSecond - firstPixelColor.blue) < DiffValueForSelection_blue) {
                        toRemove = true;
                    }
                }
            }
            if(isRedChecked){
                int redValueSecond = Color.red(bitmap2.getPixel(firstPixelColor.x, firstPixelColor.y));
                if(isSelectionIncreasing_red) {
                    if ((firstPixelColor.red - redValueSecond) < DiffValueForSelection_red) {
                        toRemove = true;
                    }
                }
                else {
                    if ((redValueSecond - firstPixelColor.red) < DiffValueForSelection_red) {
                        toRemove = true;
                    }
                }
            }
            if(isGreenChecked){
                int greenValueSecond = Color.green(bitmap2.getPixel(firstPixelColor.x, firstPixelColor.y));
                if(isSelectionIncreasing_green) {
                    if ((firstPixelColor.green - greenValueSecond) < DiffValueForSelection_green) {
                        toRemove = true;
                    }
                }
                else {
                    if ((greenValueSecond - firstPixelColor.green) < DiffValueForSelection_green) {
                        toRemove = true;
                    }
                }
            }

            if(!toRemove)
                newColorArray.add(firstPixelColor);
        }

        MainActivity.this.runOnUiThread(() -> {
            resInfo.setText(String.format("Result:\nImage size: %sx%s\nPixels marked: %s",
                    bitmap1.getWidth(), bitmap1.getWidth(), newColorArray.size()));
            if (newColorArray.size() != 0) drawpoint();

            main_progress.setVisibility(View.GONE);
            txtUpdate.setText("Analyse completed");
        });

    }

    private void drawpoint() {
        //Create a new image bitmap and attach a brand new canvas to it
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);

        tempCanvas.drawBitmap(bitmap1, 0, 0, null);

        Log.e("length", String.valueOf(newColorArray.size()));

        for (PixelColor pixelColor : newColorArray) {
//            Log.e("x y", String.format("%s %s", pixelColor.x, pixelColor.y));
//Draw everything else you want into the canvas, in this example a rectangle with rounded edges
//            tempCanvas.drawRoundRect(new RectF(pixelColor.x,pixelColor.y,pixelColor.x+100,
//                    pixelColor.y+100), 50, 50, paint);
            tempCanvas.drawPoint(pixelColor.x, pixelColor.y, paint);
        }

//Attach the canvas to the ImageView
        main_image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    private Bitmap getBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri), new ImageDecoder.OnHeaderDecodedListener() {
                    @Override
                    public void onHeaderDecoded(@NonNull ImageDecoder imageDecoder, @NonNull ImageDecoder.ImageInfo imageInfo, @NonNull ImageDecoder.Source source) {
                        imageDecoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                        imageDecoder.isMutableRequired();
                    }
                });
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    private void dispatchTakePictureIntent() {
    }

    private void inflateFragment() {
        //begin fragment
        cameraFragment = new CameraFragment();
        cameraFragment.setFragmentCallback(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.add(R.id.main_frame, cameraFragment, "myTag").commit();

    }
}

