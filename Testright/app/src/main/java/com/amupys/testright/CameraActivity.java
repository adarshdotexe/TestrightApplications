package com.amupys.testright;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private AutoFitTextureView textureView;

    // Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;

    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraCaptureSession.CaptureCallback captureCallback;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    EditText iso;
    EditText exp;
    EditText foc;
    EditText r;
    EditText g0;
    EditText g1;
    EditText b;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        prefs = getSharedPreferences("Camera Settings", MODE_PRIVATE);
        editor = prefs.edit();
        textureView = findViewById(R.id.textureview_camera);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        buttons();
        texts();
        Button btnCapture = findViewById(R.id.button_capture);
        btnCapture.setOnClickListener(view -> takePicture());
        Button auto_populate = findViewById(R.id.auto_values);
        auto_populate.setOnClickListener(view -> auto());
        Button btnBack = findViewById(R.id.button_back);
        btnBack.setOnClickListener(view -> onBackPressed());

    }

    private void takePicture() {
        if (cameraDevice == null)
            return;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            // Capture image with custom size
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            captureRequestBuilder.addTarget(reader.getSurface());

            // Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            file = createImageFile();
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        {
                            if (image != null)
                                image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        outputStream.write(bytes);
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Toast.makeText(CameraActivity.this, "Saved " + file, Toast.LENGTH_SHORT).show();

                    Intent returnIntent = new Intent();
                    returnIntent.setData(Uri.fromFile(file));
                    setResult(CameraActivity.RESULT_OK, returnIntent);
                    finish();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureRequestBuilder.build(), captureListener,
                                mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);

        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, prefs.getBoolean("autoexp", true) ? CaptureRequest.CONTROL_AE_MODE_ON : CaptureRequest.CONTROL_AWB_MODE_OFF);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, prefs.getBoolean("awb", true) ? CaptureRequest.CONTROL_AWB_MODE_AUTO : CaptureRequest.CONTROL_AWB_MODE_OFF);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, prefs.getBoolean("autofoc", true) ? CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE : CaptureRequest.CONTROL_AF_MODE_OFF);
            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.parseLong(prefs.getString("exp", "33333333")));
            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.parseInt(prefs.getString("iso", "800")));
            captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.parseFloat(prefs.getString("foc", "1.0")));
            RggbChannelVector init = new RggbChannelVector(
                    Float.parseFloat(prefs.getString("r", "1.0")),
                    Float.parseFloat(prefs.getString("g0", "0.5")),
                    Float.parseFloat(prefs.getString("g1", "0.5")),
                    Float.parseFloat(prefs.getString("b", "1")));
            captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, init);


            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null)
                                return;
                            cameraCaptureSessions = cameraCaptureSession;
                            updatePreview();
                            auto();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(CameraActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            textureView.setAspectRatio(imageDimension.getHeight(), imageDimension.getWidth());

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        return image;
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        editor.putString("iso", iso.getText().toString());
        editor.putString("exp", exp.getText().toString());
        editor.putString("foc", foc.getText().toString());
        editor.putString("r", r.getText().toString());
        editor.putString("g0", g0.getText().toString());
        editor.putString("g1", g1.getText().toString());
        editor.putString("b", b.getText().toString());
        editor.apply();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void buttons() {
        SwitchCompat exp = findViewById(R.id.autoexp);
        SwitchCompat focus = findViewById(R.id.autofocus);
        SwitchCompat awb = findViewById(R.id.awb);

        exp.setChecked(prefs.getBoolean("autoexp", true));
        focus.setChecked(prefs.getBoolean("autofoc", true));
        awb.setChecked(prefs.getBoolean("awb", true));

        exp.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                editor.putBoolean("autoexp", true);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            } else {
                editor.putBoolean("autoexp", false);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            }
            updatePreview();
        });

        focus.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                editor.putBoolean("autofoc", true);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                editor.putBoolean("autofoc", false);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            }
            updatePreview();

        });

        awb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                editor.putBoolean("awb", true);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            } else {
                editor.putBoolean("awb", false);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
            }
            updatePreview();

        });

    }

    private void texts() {
        iso = findViewById(R.id.edit_iso);
        exp = findViewById(R.id.edit_exp);
        foc = findViewById(R.id.edit_foc);
        r = findViewById(R.id.r);
        g0 = findViewById(R.id.g0);
        g1 = findViewById(R.id.g1);
        b = findViewById(R.id.b);

        iso.setText(prefs.getString("iso", "800"));
        exp.setText(prefs.getString("exp", "3333333"));
        foc.setText(prefs.getString("foc", "1"));
        r.setText(prefs.getString("r", "1.0"));
        g0.setText(prefs.getString("g0", "0.5"));
        g1.setText(prefs.getString("g1", "0.5"));
        b.setText(prefs.getString("b", "1.0"));

        iso.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals(""))
                    return;
                captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.parseInt(editable.toString()));
                updatePreview();
            }
        });

        exp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals(""))
                    return;
                captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.parseLong(editable.toString()));
                updatePreview();
            }
        });

        foc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals(""))
                    return;
                captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.parseFloat(editable.toString()));
                updatePreview();
            }
        });

        TextWatcher rggb = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    return;
                }
                float red = Float.parseFloat(r.getText().toString());
                float greenodd = Float.parseFloat(g0.getText().toString());
                float greeneven = Float.parseFloat(g1.getText().toString());
                float blue = Float.parseFloat(b.getText().toString());
                captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS,
                        new RggbChannelVector(red, greenodd, greeneven, blue));
                updatePreview();
            }
        };
        r.addTextChangedListener(rggb);
        g0.addTextChangedListener(rggb);
        g1.addTextChangedListener(rggb);
        b.addTextChangedListener(rggb);

    }

    private void auto() {

        captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                runOnUiThread(() -> {
                    exp.setText(result.get(CaptureResult.SENSOR_EXPOSURE_TIME).toString());
                    foc.setText(result.get(CaptureResult.LENS_FOCUS_DISTANCE).toString());
                    iso.setText(result.get(CaptureResult.SENSOR_SENSITIVITY).toString());
                    r.setText(String.format("%.2f", result.get(CaptureResult.COLOR_CORRECTION_GAINS).getRed()));
                    g0.setText(String.format("%.2f", result.get(CaptureResult.COLOR_CORRECTION_GAINS).getGreenOdd()));
                    g1.setText(String.format("%.2f", result.get(CaptureResult.COLOR_CORRECTION_GAINS).getGreenEven()));
                    b.setText(String.format("%.2f", result.get(CaptureResult.COLOR_CORRECTION_GAINS).getBlue()));
                });
                updatePreview();
            }
        };
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        editor.putString("iso", iso.getText().toString());
        editor.putString("exp", exp.getText().toString());
        editor.putString("foc", foc.getText().toString());
        editor.putString("r", r.getText().toString());
        editor.putString("g0", g0.getText().toString());
        editor.putString("g1", g1.getText().toString());
        editor.putString("b", b.getText().toString());
        editor.apply();
        super.finish();
    }

}