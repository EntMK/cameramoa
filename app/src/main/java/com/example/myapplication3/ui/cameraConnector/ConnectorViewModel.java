package com.example.myapplication3.ui.cameraConnector;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectorViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ConnectorViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}