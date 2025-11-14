package com.flamapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;
    
    private GLSurfaceView glSurfaceView;
    private TextView fpsTextView;
    private Button toggleButton;
    
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    
    private GLRenderer renderer;
    private int processingMode = 1; // 0=grayscale, 1=canny
    
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private float currentFPS = 0.0f;
    
    static {
        System.loadLibrary("flamapp_native");
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
        glSurfaceView = findViewById(R.id.glSurfaceView);
        fpsTextView = findViewById(R.id.fpsTextView);
        toggleButton = findViewById(R.id.toggleButton);
        
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new GLRenderer(this);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        toggleButton.setOnClickListener(v -> {
            processingMode = (processingMode + 1) % 2;
            String mode = processingMode == 0 ? "Grayscale" : "Canny Edge";
            Toast.makeText(this, "Mode: " + mode, Toast.LENGTH_SHORT).show();
        });
        
        if (checkCameraPermission()) {
            setupCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void setupCamera() {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        startBackgroundThread();
        openCamera();
    }
    
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    
    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void openCamera() {
        try {
            String cameraId = getCameraId();
            if (cameraId == null) {
                Toast.makeText(this, "No camera available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            cameraManager.openCamera(cameraId, cameraStateCallback, backgroundHandler);
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    private String getCameraId() throws CameraAccessException {
        for (String cameraId : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                return cameraId;
            }
        }
        return cameraManager.getCameraIdList().length > 0 ? cameraManager.getCameraIdList()[0] : null;
    }
    
    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }
        
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }
        
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };
    
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = new SurfaceTexture(0);
            texture.setDefaultBufferSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);
            
            final CaptureRequest.Builder captureRequestBuilder = 
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            
            cameraDevice.createCaptureSession(Arrays.asList(surface), 
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            
                            captureSession = session;
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            
                            try {
                                session.setRepeatingRequest(captureRequestBuilder.build(), 
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                            
                            startFrameProcessing(texture);
                        }
                        
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(CameraActivity.this, "Configuration failed", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, backgroundHandler);
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void startFrameProcessing(SurfaceTexture texture) {
        texture.setOnFrameAvailableListener(surfaceTexture -> {
            surfaceTexture.updateTexImage();
            
            // In a real implementation, extract frame data from SurfaceTexture
            // For this example, we'll simulate with a byte array
            byte[] frameData = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 4];
            
            // Process frame through JNI
            byte[] processedData = NativeProcessor.processFrame(
                    frameData, PREVIEW_WIDTH, PREVIEW_HEIGHT, processingMode);
            
            if (processedData != null) {
                renderer.updateTexture(processedData, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                glSurfaceView.requestRender();
                updateFPS();
            }
        }, backgroundHandler);
    }
    
    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
            return;
        }
        
        long elapsed = currentTime - lastFrameTime;
        if (elapsed >= 1000) {
            currentFPS = (frameCount * 1000.0f) / elapsed;
            frameCount = 0;
            lastFrameTime = currentTime;
            
            runOnUiThread(() -> fpsTextView.setText(
                    String.format(getString(R.string.fps_label), currentFPS)));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
        if (cameraDevice == null && checkCameraPermission()) {
            startBackgroundThread();
            openCamera();
        }
    }
    
    @Override
    protected void onPause() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        stopBackgroundThread();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
        super.onPause();
    }
}
