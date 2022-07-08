package com.example.myapplication3.ui.admin;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication3.databinding.FragmentAdminBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;
    Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AdminViewModel homeViewModel =
                new ViewModelProvider(this).get(AdminViewModel.class);

        binding = FragmentAdminBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mContext = container.getContext();
        String[] items = {"joy", "surprise", "sadness", "anger", "fear", "displeasure", "neutral"};
        Spinner emotionSpinner = binding.emotionSpinner;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, items);
        emotionSpinner.setAdapter(adapter);
        emotionSpinner.getSelectedItem();

        EditText modelIdEdit = binding.modelId;

        EditText sexEdit = binding.sex;

        EditText ageEdit = binding.age;



        Button takePhoto = binding.cameraButton;
        takePhoto.setOnClickListener(view -> {
            Thread th;
            String ageStr = ageEdit.getText().toString();
            String modelIdStr = modelIdEdit.getText().toString();
            String sexStr = sexEdit.getText().toString();
            long currentTime = System.currentTimeMillis();
            String currentTimeStr = String.valueOf(currentTime);

            String fileName = modelIdStr + "_" + sexStr + "_" + ageStr + "_" + emotionSpinner.getSelectedItem() + "_" + currentTimeStr + "_";
            Log.d("FileName", fileName);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("value", 1);
                jsonObject.accumulate("file_name", fileName);

            }catch (JSONException e) {
                e.printStackTrace();
            }
            String json = jsonObject.toString();
            Log.d("JSon", json);
            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = null;
                        url = new URL("http://54.180.109.99:8000/camera/create_info");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        if (conn != null) {
                            conn.setConnectTimeout(10000);
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            conn.setRequestProperty("Accept", "application/json");
                            conn.setRequestProperty("Content-type", "application/json");

                            OutputStream os = conn.getOutputStream();
                            os.write(json.getBytes("euc-kr"));
                            os.flush();

                            int res = conn.getResponseCode();
                            Log.d("TEST", Integer.toString(res));
                            conn.disconnect();
                            Log.d("TEST", "END");
                            Handler mHandler = new Handler(Looper.getMainLooper());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "사진 촬영을 2초후 시작합니다..", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "사진 촬영이 실패했습니다..", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            th.start();
        });

        Button saveButton = binding.saveButton;
        saveButton.setOnClickListener(view -> {
            Thread th;
            String ageStr = ageEdit.getText().toString();
            String modelIdStr = modelIdEdit.getText().toString();
            String sexStr = sexEdit.getText().toString();

            String fileName = modelIdStr + "_" + sexStr + "_" + ageStr + "_" + emotionSpinner.getSelectedItem();
            Log.d("FileName", fileName);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("value", 0);
                jsonObject.accumulate("file_name", fileName);

            }catch (JSONException e) {
                e.printStackTrace();
            }
            String json = jsonObject.toString();
            Log.d("JSon", json);
            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = null;
                        url = new URL("http://54.180.109.99:8000/camera/create_info");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        if (conn != null) {
                            conn.setConnectTimeout(10000);
                            conn.setRequestMethod("POST");

                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            conn.setRequestProperty("Accept", "application/json");
                            conn.setRequestProperty("Content-type", "application/json");

                            OutputStream os = conn.getOutputStream();
                            os.write(json.getBytes("euc-kr"));
                            os.flush();

                            int res = conn.getResponseCode();
                            Log.d("TEST", Integer.toString(res));
                            conn.disconnect();
                            Log.d("TEST", "END");
                            Handler mHandler = new Handler(Looper.getMainLooper());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "사진 촬영을 2초후 종료합니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "사진 촬영 종료에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            th.start();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}