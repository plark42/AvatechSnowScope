package com.example.avatechsnowscope;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickViewActiveProbe(View view) {
        Intent intent = new Intent(this, BluetoothScanActivity.class);
        startActivity(intent);
    }

    public void onClickManageProbeData(View view) {
        Intent intent = new Intent(this, ManageProbeDataActivity.class);
        startActivity(intent);
    }

    public void onClickSyncProbeData(View view) {
    }
}
