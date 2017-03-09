package com.lzx.lock.module.camera;

import android.Manifest;
import android.app.Fragment;
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
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioAttributes;
import android.media.Image;
import android.media.ImageReader;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzx.lock.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public class Camera2Fragment extends Fragment {
    private static final String TAG = "Camera2Fragment";
    private static final int SETIMAGE = 1;
    private static final int MOVE_FOCK = 2;

    TextureView mTextureView;
    ImageView mThumbnail;
    Button mButton;
    Handler mHandler;
    Handler mUIHandler;
    ImageReader mImageReader;
    CaptureRequest.Builder mPreViewBuidler;
    CameraCaptureSession mCameraSession;
    CameraCharacteristics mCameraCharacteristics;
    Ringtone ringtone;
    //相机会话的监听器，通过他得到mCameraSession对象，这个对象可以用来发送预览和拍照请求
    private CameraCaptureSession.StateCallback mSessionStateCallBack = new CameraCaptureSession
            .StateCallback() {
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
    private Surface surface;
    //打开相机时候的监听器，通过他可以得到相机实例，这个实例可以创建请求建造者
    private CameraDevice.StateCallback cameraOpenCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d(TAG, "相机已经打开");
            try {
                mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                texture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
                surface = new Surface(texture);
                mPreViewBuidler.addTarget(surface);
                cameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface
                        ()), mSessionStateCallBack, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d(TAG, "相机连接断开");
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Log.d(TAG, "相机打开失败");
        }
    };
    private ImageReader.OnImageAvailableListener onImageAvaiableListener = new ImageReader
            .OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            mHandler.post(new ImageSaver(imageReader.acquireNextImage()));
        }
    };
    private Size mPreViewSize;
    private Rect maxZoomrect;
    private int maxRealRadio;

    //预览图显示控件的监听器，可以监听这个surface的状态
    private TextureView.SurfaceTextureListener mSurfacetextlistener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            HandlerThread thread = new HandlerThread("Ceamera3");
            thread.start();
            mHandler = new Handler(thread.getLooper());
            CameraManager manager = (CameraManager) getActivity().getSystemService(Context
                    .CAMERA_SERVICE);
            String cameraid = CameraCharacteristics.LENS_FACING_FRONT + "";
            try {
                mCameraCharacteristics = manager.getCameraCharacteristics(cameraid);

                //画面传感器的面积，单位是像素。
                maxZoomrect = mCameraCharacteristics.get(CameraCharacteristics
                        .SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                //最大的数字缩放
                maxRealRadio = mCameraCharacteristics.get(CameraCharacteristics
                        .SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
                picRect = new Rect(maxZoomrect);

                StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics
                        .SCALER_STREAM_CONFIGURATION_MAP);
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)
                ), new CompareSizeByArea());
                mPreViewSize = map.getOutputSizes(SurfaceTexture.class)[0];
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 5);
                mImageReader.setOnImageAvailableListener(onImageAvaiableListener, mHandler);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                mButton.setOnTouchListener(onTouchListener);
            } catch (CameraAccessException e) {
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

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        mCameraSession.setRepeatingRequest(initDngBuilder().build(), null, mHandler);
                    } catch (CameraAccessException e) {
                        Toast.makeText(getActivity(), "请求相机权限被拒绝", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    try {
                        updateCameraPreviewSession();
                    } catch (CameraAccessException e) {
                        Toast.makeText(getActivity(), "请求相机权限被拒绝", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        }
    };

    private void updateCameraPreviewSession() throws CameraAccessException {
        mPreViewBuidler.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreViewBuidler.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        mCameraSession.setRepeatingRequest(mPreViewBuidler.build(), null, mHandler);
    }

    /**
     * 设置连拍的参数
     *
     * @return
     */
    private CaptureRequest.Builder initDngBuilder() {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

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
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            String cameraid = CameraCharacteristics.LENS_FACING_FRONT + "";
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraid);
            Range<Integer> fps[] = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);
        } catch (CameraAccessException e) {
            Toast.makeText(getActivity(), "请求相机权限被拒绝", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "打开相机失败", Toast.LENGTH_SHORT).show();
        }
        return captureBuilder;
    }

    private View.OnClickListener picOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                shootSound();
                Log.d(TAG, "正在拍照");
                CaptureRequest.Builder builder = mCameraSession.getDevice().createCaptureRequest
                        (CameraDevice.TEMPLATE_STILL_CAPTURE);
                builder.addTarget(mImageReader.getSurface());
                builder.set(CaptureRequest.SCALER_CROP_REGION, picRect);
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_EDOF);
//                builder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                        CameraMetadata.CONTROL_AF_TRIGGER_START);
                builder.set(CaptureRequest.JPEG_ORIENTATION, 90);
                mCameraSession.capture(builder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    };
    private View.OnTouchListener textTureOntuchListener = new View.OnTouchListener() {
        //时时当前的zoom
        public double zoom;
        // 0<缩放比<mCameraCharacteristics.get(CameraCharacteristics
        // .SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
        //上次缩放前的zoom
        public double lastzoom;
        //两个手刚一起碰到手机屏幕的距离
        public double lenth;
        int count;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    count = 1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (count >= 2) {
                        float x1 = event.getX(0);
                        float y1 = event.getY(0);
                        float x2 = event.getX(1);
                        float y2 = event.getY(1);
                        float x = x1 - x2;
                        float y = y1 - y2;
                        Double lenthRec = Math.sqrt(x * x + y * y) - lenth;
                        Double viewLenth = Math.sqrt(v.getWidth() * v.getWidth() + v.getHeight()
                                * v.getHeight());
                        zoom = ((lenthRec / viewLenth) * maxRealRadio) + lastzoom;
                        picRect.top = (int) (maxZoomrect.top / (zoom));
                        picRect.left = (int) (maxZoomrect.left / (zoom));
                        picRect.right = (int) (maxZoomrect.right / (zoom));
                        picRect.bottom = (int) (maxZoomrect.bottom / (zoom));
                        Message.obtain(mUIHandler, MOVE_FOCK).sendToTarget();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    count = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    count++;
                    if (count == 2) {
                        float x1 = event.getX(0);
                        float y1 = event.getY(0);
                        float x2 = event.getX(1);
                        float y2 = event.getY(1);
                        float x = x1 - x2;
                        float y = y1 - y2;
                        lenth = Math.sqrt(x * x + y * y);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    count--;
                    if (count < 2)
                        lastzoom = zoom;
                    break;
            }
            return true;
        }
    };
    //相机缩放相关
    private Rect picRect;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera2, null);
        findview(v);
        mUIHandler = new Handler(new InnerCallBack());
        //初始化拍照的声音
        ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse
                ("file:///system/media/audio/ui/camera_click.ogg"));
        AudioAttributes.Builder attr = new AudioAttributes.Builder();
        attr.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
        ringtone.setAudioAttributes(attr.build());
        //初始化相机布局
        mTextureView.setSurfaceTextureListener(mSurfacetextlistener);
        mTextureView.setOnTouchListener(textTureOntuchListener);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCameraSession != null) {
            mCameraSession.getDevice().close();
            mCameraSession.close();
        }
    }

    private void findview(View v) {
        mTextureView = (TextureView) v.findViewById(R.id.tv_textview);
        mButton = (Button) v.findViewById(R.id.btn_takepic);
        mThumbnail = (ImageView) v.findViewById(R.id.iv_Thumbnail);
        mThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "别戳了，那个页面还没写", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 播放系统的拍照的声音
     */
    public void shootSound() {
        ringtone.stop();
        ringtone.play();
    }

    private class ImageSaver implements Runnable {
        Image reader;

        public ImageSaver(Image reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            Log.d(TAG, "正在保存图片");
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .getAbsoluteFile();
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
                Log.d(TAG, "保存图片完成");
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
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case SETIMAGE:
                    Bitmap bm = (Bitmap) message.obj;
                    mThumbnail.setImageBitmap(bm);
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
}
