package com.example.myapplication3.ui.cameraConnector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication3.databinding.FragmentCameraConnectorBinding;
import com.example.myapplication3.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraConnectorFragment extends Fragment {

    private FragmentCameraConnectorBinding binding;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1002;
    private final int MY_PERMISSIONS_REQUEST_AUDIO = 1003;
    Context mContext = null;
    public WifiP2pManager mManager;
    public WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConnectorViewModel connectorViewModel =
                new ViewModelProvider(this).get(ConnectorViewModel.class);

        binding = FragmentCameraConnectorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Context 설정
        mContext = container.getContext();

        // 와이파이 다이렉트 설정
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //현 장비의 상황 변화
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // 사진 권한 체크
        int permissionCheck = ContextCompat.checkSelfPermission(container.getContext(), Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(container.getContext(), "권한 승인이 필요합니다",
                    Toast.LENGTH_LONG).show();
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) container.getContext(), Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions((Activity) container.getContext(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
            Toast.makeText(container.getContext(), "사용을 위해 카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show();
        }
        permissionCheck = ContextCompat.checkSelfPermission(container.getContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(container.getContext(), "권한 승인이 필요합니다",
                    Toast.LENGTH_LONG).show();
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) container.getContext(), Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions((Activity) container.getContext(), new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_AUDIO);
            }
            Toast.makeText(container.getContext(), "사용을 위해 오디오 권한이 필요합니다.", Toast.LENGTH_LONG).show();
        }

        // 카메라 번호 적용과 저장
        EditText cameraNumberEdit = binding.cameraNumberEdit;
        String cameraNumberStr = Utils.getCameraNumber(container.getContext());
        Log.d("CameraNumber", cameraNumberStr);
        cameraNumberEdit.setText(cameraNumberStr);

        Button saveButton = binding.saveButton;
        saveButton.setOnClickListener(view -> {
            EditText cameraNumber = binding.cameraNumberEdit;
            String cameraNumberStr1 = cameraNumber.getText().toString();
            Log.d("Save", cameraNumberStr1);
            Utils.saveCameraNumber(cameraNumberStr1, container.getContext());
        });




        return root;
    }

    @Override
    public void onResume () {
        super.onResume();
    }

    @Override
    public void onPause () {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}