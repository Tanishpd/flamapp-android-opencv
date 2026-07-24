package com.flamapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
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

import java.nio.ByteBuffer;
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
    private ImageReader imageReader;
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
            // Frames are delivered through an ImageReader rather than a
            // SurfaceTexture. The old code did `new SurfaceTexture(0)`, binding to GL
            // texture name 0 — not a real texture in the renderer's context — so
            // updateTexImage() could never have produced usable pixels.
            imageReader = ImageReader.newInstance(
                    PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(this::onImageAvailable, backgroundHandler);
            Surface surface = imageReader.getSurface();

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
    
    /**
     * Called on the background thread each time the camera delivers a frame.
     * Converts YUV_420_888 to the RGBA layout the native processor expects,
     * runs it through OpenCV, and hands the result to the GL renderer.
     */
    private void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }
        try {
            byte[] frameData = yuv420ToRgba(image);
            byte[] processedData = NativeProcessor.processFrame(
                    frameData, image.getWidth(), image.getHeight(), processingMode);

            if (processedData != null) {
                renderer.updateTexture(processedData, image.getWidth(), image.getHeight());
                glSurfaceView.requestRender();
                updateFPS();
            }
        } finally {
            // Must close, or the reader starves after `maxImages` frames.
            image.close();
        }
    }

    /**
     * YUV_420_888 to RGBA_8888.
     *
     * The planes cannot be read as flat arrays: the U and V planes carry both a
     * rowStride and a pixelStride (pixelStride is 2 on the common semi-planar
     * NV21/NV12 layouts, where U and V are interleaved), and rowStride is often
     * wider than the image. Both are honoured here.
     *
     * Conversion is BT.601 full-range.
     */
    private static byte[] yuv420ToRgba(Image image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final Image.Plane yPlane = image.getPlanes()[0];
        final Image.Plane uPlane = image.getPlanes()[1];
        final Image.Plane vPlane = image.getPlanes()[2];

        final ByteBuffer yBuf = yPlane.getBuffer();
        final ByteBuffer uBuf = uPlane.getBuffer();
        final ByteBuffer vBuf = vPlane.getBuffer();

        final int yRowStride = yPlane.getRowStride();
        final int uvRowStride = uPlane.getRowStride();
        final int uvPixelStride = uPlane.getPixelStride();

        final byte[] out = new byte[width * height * 4];

        for (int row = 0; row < height; row++) {
            final int uvRow = (row >> 1) * uvRowStride;
            final int yRow = row * yRowStride;

            for (int col = 0; col < width; col++) {
                final int y = (yBuf.get(yRow + col) & 0xFF);
                final int uvIndex = uvRow + (col >> 1) * uvPixelStride;

                final int u = (uBuf.get(uvIndex) & 0xFF) - 128;
                final int v = (vBuf.get(uvIndex) & 0xFF) - 128;

                int r = (int) (y + 1.402f * v);
                int g = (int) (y - 0.344136f * u - 0.714136f * v);
                int b = (int) (y + 1.772f * u);

                final int o = (row * width + col) * 4;
                out[o]     = (byte) (r < 0 ? 0 : (r > 255 ? 255 : r));
                out[o + 1] = (byte) (g < 0 ? 0 : (g > 255 ? 255 : g));
                out[o + 2] = (byte) (b < 0 ? 0 : (b > 255 ? 255 : b));
                out[o + 3] = (byte) 255;
            }
        }
        return out;
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
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        stopBackgroundThread();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
        super.onPause();
    }
}
