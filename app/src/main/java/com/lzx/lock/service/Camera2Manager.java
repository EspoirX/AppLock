package com.lzx.lock.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.lzx.lock.utils.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

/**
 * 参考：https://github.com/miqt/camera2
 * Created by lzx on 2017/3/9.
 * 386707112@qq.com
 */

public class Camera2Manager {

    private static final int SETIMAGE = 1;
    private static final int MOVE_FOCK = 2;

    private TextureView mTextureView;
    private Context mContext;

    private Handler mHandler;
    private Handler mUIHandler;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreViewBuidler;
    private CameraCaptureSession mCameraSession;
    private CameraCharacteristics mCameraCharacteristics;
    private Size mPreViewSize;
    private Rect maxZoomrect;
    private int maxRealRadio;
    //相机缩放相关
    private Rect picRect;

    public Camera2Manager(TextureView textureView, Context context) {
        mTextureView = textureView;
        mContext = context;
    }

    public void initCamera() {
        mUIHandler = new Handler(new InnerCallBack());
        mTextureView.setSurfaceTextureListener(mSurfacetextlistener);
    }

    @SuppressLint("NewApi")
    private ImageReader.OnImageAvailableListener onImageAvaiableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            mHandler.post(new ImageSaver(imageReader.acquireNextImage()));
        }
    };

    private Surface surface;
    @SuppressLint("NewApi")
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            try {
                mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                texture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
                surface = new Surface(texture);
                mPreViewBuidler.addTarget(surface);
                cameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mSessionStateCallBack, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {

        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }
    };

    @SuppressLint("NewApi")
    private CameraCaptureSession.StateCallback mSessionStateCallBack = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            try {
                mCameraSession = cameraCaptureSession;
                cameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

        }
    };

    private TextureView.SurfaceTextureListener mSurfacetextlistener = new TextureView
            .SurfaceTextureListener() {
        @SuppressLint("NewApi")
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            HandlerThread thread = new HandlerThread("Camera2");
            thread.start();
            mHandler = new Handler(thread.getLooper());
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            String cameraid = CameraCharacteristics.LENS_FACING_FRONT + "";
            try {
                mCameraCharacteristics = manager.getCameraCharacteristics(cameraid);

                //画面传感器的面积，单位是像素。
                maxZoomrect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                //最大的数字缩放
                maxRealRadio = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
                picRect = new Rect(maxZoomrect);
                StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)
                ), new CompareSizeByArea());
                mPreViewSize = map.getOutputSizes(SurfaceTexture.class)[0];
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 5);
                mImageReader.setOnImageAvailableListener(onImageAvaiableListener, mHandler);
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.openCamera(cameraid, cameraOpenCallBack, mHandler);
                //设置点击拍照的监听
                takePhoto();
            } catch (CameraAccessException e) {
                ToastUtil.showToast(e.getMessage());
                e.printStackTrace();
            }
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

    @SuppressLint("NewApi")
    private void takePhoto() {
        try {
            mCameraSession.setRepeatingRequest(initDngBuilder().build(), null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private CaptureRequest.Builder initDngBuilder() {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.addTarget(surface);
            // Required for RAW capture
            captureBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (long) ((214735991 - 13231) / 2));
            captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, (10000 - 100) / 2);//设置 ISO，感光度
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            //设置每秒30帧
            CaptureRequest mCaptureRequest = captureBuilder.build();
            mCameraSession.capture(mCaptureRequest, null, mHandler); //拍照
//            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
//            String cameraid = CameraCharacteristics.LENS_FACING_FRONT + "";
//            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraid);
//            Range<Integer> fps[] = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
//            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return captureBuilder;
    }

    private class ImageSaver implements Runnable {
        Image reader;

        public ImageSaver(Image reader) {
            this.reader = reader;
        }

        @SuppressLint("NewApi")
        @Override
        public void run() {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                ByteBuffer buffer = reader.getPlanes()[0].getBuffer();
                byte[] buff = new byte[buffer.remaining()];
                buffer.get(buff);
                BitmapFactory.Options ontain = new BitmapFactory.Options();
                ontain.inSampleSize = 50;
                Bitmap bm = BitmapFactory.decodeByteArray(buff, 0, buff.length, ontain);
                Message.obtain(mUIHandler, SETIMAGE, bm).sendToTarget();
                outputStream.write(buff);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class InnerCallBack implements Handler.Callback {
        @SuppressLint("NewApi")
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case SETIMAGE:
                    Bitmap bm = (Bitmap) message.obj;
                    //预览头像
                    break;
                case MOVE_FOCK:
                    mPreViewBuidler.set(CaptureRequest.SCALER_CROP_REGION, picRect);
                    try {
                        mCameraSession.setRepeatingRequest(mPreViewBuidler.build(), null,
                                mHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return false;
        }
    }

    @SuppressLint("NewApi")
    public class CompareSizeByArea implements java.util.Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}
