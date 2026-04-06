package com.example.menureader.Front;

import android.os.Bundle;
import com.example.menureader.LogHandler;
import com.example.menureader.R;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    /**
     * Gets called at the launch of MenuReader
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHandler.m("In Main Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}