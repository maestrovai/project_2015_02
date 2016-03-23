package com.irgups.sabina.irgupsinfo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

/**
 * Класс preview камеры
 * Исмайлова Сабина ПО-10-1 06.15
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;
    private byte[] cameraBuffer;

    public CameraPreview(Context context, Camera camera,
                         PreviewCallback previewCb,
                         AutoFocusCallback autoFocusCb) {
        super(context);
        mCamera = camera;
        previewCallback = previewCb;
        autoFocusCallback = autoFocusCb;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // для версий Android ниже 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // Surface создан, объявляем камере где создавайть preview.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException e) {
            Log.d("DBG", "Ошибка превью камеры: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // уничтожение Camera preview
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            //если  preview surface не существует
            return;
        }
        if (mCamera != null) {
            // остановка preview до вненсения изменений
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
            }

            try {
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size previewSize = parameters.getPreviewSize();
                int imageFormat = parameters.getPreviewFormat();
                int bufferSize = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(imageFormat) / 8;
                cameraBuffer = new byte[bufferSize];

                // Поворот preview камеры на 90 градусов для Activity портретной ориентации
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setPreviewCallbackWithBuffer(null);
                mCamera.setPreviewCallbackWithBuffer(previewCallback);
                mCamera.addCallbackBuffer(cameraBuffer);
                mCamera.startPreview();
                mCamera.autoFocus(autoFocusCallback);
            } catch (Exception e) {
                Log.d("DBG", "Ошибка превью камеры: " + e.getMessage());
            }
        }
    }
}
