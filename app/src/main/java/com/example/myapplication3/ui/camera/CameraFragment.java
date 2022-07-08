package com.example.myapplication3.ui.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.hardware.Camera;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication3.databinding.FragmentCameraBinding;
import com.example.myapplication3.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;
    private TextureView mTextureView;

    private CameraDevice mCamera;
    private Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Context mContext;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    Semaphore mSemaphore = new Semaphore(1);
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    MediaRecorder mMediaRecorder;

    private String mNextVideoAbsolutePath = "";
    Size mVideoSize;
    private boolean mIsRecordingVideo;

    int mSensorOrientation;
    CameraCaptureSession mCameraCaptureSession;
    boolean isClick = false;
    boolean waitStart = false;

    boolean cameraState = false;
    SwitchCompat connectSwitch;
    final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    Thread th;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CameraViewModel cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);


        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mMediaRecorder = new MediaRecorder();
        mTextureView = binding.textureView;
        mContext = container.getContext();
        initTextureView();

        connectSwitch = binding.cameraConnectSwitch;
        connectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Check", "Check");
                    try {
                        th = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while(true) {
                                        if (Thread.interrupted()) {
                                            Log.d("Interrupted!", "Inter");
                                            break;
                                        }
                                        URL url = null;
                                        url = new URL("http://54.180.109.99:8000/state");
                                        Log.d("StartTime", String.valueOf(System.currentTimeMillis()));
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        Log.d("EndTime", String.valueOf(System.currentTimeMillis()));
                                        final StringBuilder sb = new StringBuilder();
                                        int startTime = (int)System.currentTimeMillis();

                                        if(conn != null) {
                                            conn.setConnectTimeout(10000);
                                            conn.setRequestMethod("GET");

                                            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                                        conn.getInputStream(), "utf-8"
                                                ));
                                                String line;
                                                while ((line = br.readLine()) != null) {
                                                    sb.append(line);
                                                }
                                                br.close();
                                                Log.i("tag", "결과문자열: " + sb.toString());
                                                Log.d("tellTime", String.valueOf(System.currentTimeMillis()));
                                                JSONObject response = new JSONObject(sb.toString());


                                                int value = response.getJSONObject("json").getInt("value");
                                                Log.d("value", String.valueOf(value));
                                                String fileName = response.getJSONObject("json").getString("file_name");
                                                Log.d("fileNAME", fileName);

                                                if (value == 1 && !cameraState) {
                                                    cameraState = true;
                                                    int endTime = (int)System.currentTimeMillis();
                                                    int duringTime = endTime - startTime;
                                                    int serverTime = response.getInt("time");
                                                    int cameraTime = response.getJSONObject("json").getInt("on_time");

                                                    Log.d("Camera ON", "Camera On");
                                                    startRecordingVideo(fileName);

                                                    isClick = true;
                                                    // Toast.makeText(mContext, "camera on", Toast.LENGTH_SHORT).show();

                                                } else if(value == 0 && cameraState) {
                                                    cameraState = false;
                                                    Log.d("Camera Off", "Camera Off");
                                                    int endTime = (int)System.currentTimeMillis();
                                                    int duringTime = endTime - startTime;
                                                    int serverTime = response.getInt("time");
                                                    int cameraTime = response.getJSONObject("json").getInt("on_time");

                                                    stopRecordingVideo();

                                                    isClick = false;
                                                    // Toast.makeText(mContext, "camera off", Toast.LENGTH_SHORT).show();
                                                }
                                                Thread.sleep(30);

                                            }
                                            conn.disconnect();
                                        }
                                    }
                                    Log.d("Thread Inter", "Thread Inter");

                                } catch (JSONException | IOException | InterruptedException e) {
                                    e.printStackTrace();
                                    Handler mHandler = new Handler(Looper.getMainLooper());
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectSwitch.setChecked(false);
                                            Toast.makeText(mContext, "인터넷 연결이 안되있습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }

                            }
                        });
                        th.start();
                    } catch (Exception e) {

                        e.printStackTrace();

                    }
                }
                else {

                    try {
                        th.interrupt();
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                    Log.d("not check", "not check");
                }
            }
        });

        return root;

    }

    private void closeCamera() {
        try {
            mSemaphore.acquire();
            closePreviewSession();
            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }
            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mSemaphore.release();
        }

    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void initTextureView() {
        Log.d("INIT", "INITTEXTTURE");
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                Log.e("cklee", "MMM onSurfaceTextureAvailable");
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                Log.e("cklee", "MMM onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.e("cklee", "MMM onSurfaceTextureDestroyed");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                // 화면 갱신시마다 불림
//                Log.e("cklee", "MMM onSurfaceTextureUpdated");
            }
        });
    }

    private void openCamera() {
        Log.d("Open Camera", "OpenCamera");
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdArray = manager.getCameraIdList();
            Log.e("cklee", "MMM cameraIds = " + Arrays.deepToString(cameraIdArray));

            // test 로 0 번 camera 를 사용

            String oneCameraId = "1";
            for(int i = 0; i < cameraIdArray.length; i++) {
                Log.d("CameraID", cameraIdArray[i]);
            }
            Log.d("Now Camera ID", oneCameraId);
            mMediaRecorder = new MediaRecorder();

            CameraCharacteristics cameraCharacter = manager.getCameraCharacteristics(oneCameraId);
            Log.e("cklee", "MMM camraCharacter = " + cameraCharacter);

            StreamConfigurationMap map = cameraCharacter.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizesForStream = map.getOutputSizes(SurfaceTexture.class);
            Log.e("cklee", "MMM sizesForStream = " + Arrays.deepToString(sizesForStream));
            Log.d("SIZEFORSTREAM", String.valueOf(sizesForStream.length));
            // 가장 큰 사이즈부터 들어있다
            for(int i = 0; i< sizesForStream.length; i++) {
                Log.d("CAMERA STREAM SIZE", String.valueOf(sizesForStream[i].getWidth()));
                Log.d("CAMERA STREAM SIZE", String.valueOf(sizesForStream[i].getHeight()));
            }
            mPreviewSize = sizesForStream[0];
            Log.d("MPREVIEWSIZE", String.valueOf(mPreviewSize.getWidth()));

            CameraCharacteristics cc = manager.getCameraCharacteristics(oneCameraId);
            StreamConfigurationMap scm = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mVideoSize = chooseVideoSize(scm.getOutputSizes(MediaRecorder.class));
            Log.d("mVideoSize", String.valueOf(mVideoSize.getWidth()));
            mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);

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

            manager.openCamera(oneCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    mCamera = cameraDevice;
                    Log.d("CALLBACK", "CALLBACK");
                    showCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    mCamera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int errorCode) {
                    Log.e("cklee", "MMM errorCode = " + errorCode);
                    mCamera.close();
                    mCamera = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("cklee", "MMM openCamera ", e);
        }
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            Log.d("CHOOSE WIDTH", String.valueOf(size.getWidth()));
            Log.d("CHOOSE HEIGHT", String.valueOf(size.getHeight()));
        }
        return choices[0];
    }

    private void showCameraPreview() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface textureViewSurface = new Surface(texture);

            mCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(textureViewSurface);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


            mCamera.createCaptureSession(Arrays.asList(textureViewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    Log.d("1", "updatePreview");

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e("cklee", "MMM onConfigureFailed");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("cklee", "MMM showCameraPreview ", e);
        }
    }

    private void updatePreview() {
        try {
            Log.d("Privew", "Privew");
            setUpCaptureRequestBuilder(mCaptureRequestBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);

            // mCameraSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            Log.e("cklee", "MMM updatePreview", e);
        }
    }
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void setUpMediaRecorded(String fileName) throws IOException {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
                mNextVideoAbsolutePath = getVideoFilePath(fileName);
            }
            mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            mMediaRecorder.setVideoFrameRate(24);
            Log.d("Video WIDTH", String.valueOf(mVideoSize.getWidth()));
            Log.d("Video Height", String.valueOf(mVideoSize.getHeight()));
            mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mMediaRecorder.setOrientationHint(90);

            mMediaRecorder.prepare();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void timer() {

    }

    private void startRecordingVideo(String fileName) {
        try {
            Log.d("STARTRECORDING", "STARTRECORDING");
            setUpMediaRecorded(fileName);
            Log.d("E", "ERROR");
            if (mCamera == null) {
                Log.d("Mcamera", "NULL");
            }
            mCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            SurfaceTexture texture = binding.textureView.getSurfaceTexture();
            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mCaptureRequestBuilder.addTarget(previewSurface);

            Surface recordSurface = mMediaRecorder.getSurface();
            surfaces.add(recordSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    Log.d("2", "updatePreview");
                    updatePreview();
                    getActivity().runOnUiThread(() -> {
                        mIsRecordingVideo = true;
                        Log.d("test", "Start");
                        mMediaRecorder.start();
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d("TEST", "Faile");
                    if (null != mContext) {
                        Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("사진촬영").setMessage("시작되었습니다.");
                    builder.create().show();
                }
            });
        } catch (IOException | CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closePreviewSession() {
        if (mCameraCaptureSession != null)
        {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }


    private void stopRecordingVideo() {
        try {
            mIsRecordingVideo = false;
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.d("PATH", mNextVideoAbsolutePath);
            File file = new File(mNextVideoAbsolutePath);

            if (!file.exists()) {
                file.mkdir();
                Log.d("MKDIR", "MKDIR");
            }
            Log.d("SAVE", "SAVE");
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            mNextVideoAbsolutePath = null;
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("사진촬영").setMessage("종료되었습니다.");
                    builder.create().show();
                }
            });
            initTextureView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stop() {


    }

    String getVideoFilePath(String fileName) {
        Log.d("FILETPATHSTRING", fileName);
        final File dir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        String path = dir.getPath() + "/DCIM/CameraMoa/";
        Log.d("FilePATH", path);
        File dst = new File(path);
        if(!dst.exists()) dst.mkdir();
        return path + fileName + Utils.getCameraNumber(mContext) + ".mp4";
    }


    private void save(byte[] bytes, File file) throws IOException {
        OutputStream outputStream = null;
        outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        isClick = false;
        cameraState = false;
        Log.d("ONPAUSE", "ONPAUSE!!");
        try {
            th.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stopRecordingVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectSwitch.setChecked(false);

    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        isClick = false;
        cameraState = false;
        initTextureView();
        Log.d("ONRESUME", "ONRESUME!!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isClick = false;
        cameraState = false;
        binding = null;
        try {
            th.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stopRecordingVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectSwitch.setChecked(false);

    }
}

