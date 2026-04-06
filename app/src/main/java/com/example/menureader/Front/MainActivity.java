package com.example.menureader.Front;

import android.os.Bundle;
import com.example.menureader.LogHandler;
import com.example.menureader.R;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHandler.m("In Main Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}