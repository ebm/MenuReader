package com.example.menureader.Front;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.example.menureader.Handling.Persistence;
import com.example.menureader.LogHandler;
import com.example.menureader.R;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    SharedViewModel svm;
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
        svm = new ViewModelProvider(this).get(SharedViewModel.class);
        Persistence.load(svm.getCache(), this);
        Persistence.loadMenuList(this, menu -> svm.addMenu(menu));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onStop() {
        super.onStop();
        new Thread(() -> {
            Persistence.save(svm.getCache(), this.getApplicationContext());
            Persistence.saveMenuList(svm.getMenuList(), this.getApplicationContext());
        }).start();
    }
}
