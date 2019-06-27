package com.example.avatechsnowscope;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothScanActivity extends AppCompatActivity {

    private ListView listViewBluetoothDevices;
    private ArrayAdapter<String> arrayAdapter;

    private ArrayList<BluetoothDevice> arrayListDevices;
    private ArrayList<String> arrayListDeviceNames;

    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;

    private static int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private final int SCAN_PERIOD = 10000;

    private final static String TAG = BluetoothScanActivity.class.getSimpleName();
    private boolean scanning;

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice bluetoothDevice = arrayListDevices.get(i);
            String bluetoothDeviceAddress = bluetoothDevice.getAddress();
            Intent intent = new Intent(getApplicationContext(), ViewActiveProbeActivity.class);
            intent.putExtra(ViewActiveProbeActivity.BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        listViewBluetoothDevices = findViewById(R.id.listViewBluetoothDevices);
        arrayListDevices = new ArrayList<BluetoothDevice>();
        arrayListDeviceNames = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListDeviceNames);

        listViewBluetoothDevices.setAdapter(arrayAdapter);
        listViewBluetoothDevices.setOnItemClickListener(onItemClickListener);

        handler = new Handler();

        Log.d(TAG, "Request Location Permissions:");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ERROR: Bluetooth LE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "ERROR: Bluetooth LE not supported!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if(requestCode == PERMISSION_REQUEST_CODE) {
            //Do something based on grantResults
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "coarse location permission granted");
            } else {
                Log.d(TAG, "coarse location permission denied");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        scan(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scan(false);
        arrayAdapter.clear();
    }

    private void scan(final boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(scanCallback);
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothAdapter.startLeScan(scanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(scanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback scanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!arrayListDevices.contains(device)) {
                                arrayListDevices.add(device);

                                String deviceName = device.getName();
                                if (deviceName != null) {
                                    deviceName += ": " + device.getAddress();
                                } else {
                                    deviceName = "UNKNOWN: " + device.getAddress();
                                }
                                arrayListDeviceNames.add(deviceName);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };
}
