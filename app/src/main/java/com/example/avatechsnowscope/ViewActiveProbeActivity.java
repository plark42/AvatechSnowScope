package com.example.avatechsnowscope;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
TODO
    - receive profile ID, write profile ID to PROFILES_TO_TRANSFER CHAR
    - read on CHARACTERISTIC_PROFILES => view String, check if serial number?
    - if so, keep reading on this one?
    - simulate return value?
 */

public class ViewActiveProbeActivity extends AppCompatActivity {

    public static final String BLUETOOTH_DEVICE_ADDRESS = "BLUETOOTH_DEVICE_ADDRESS";

    private TextView textViewProbeStatus;
    private TextView textViewProfileId;
    private GraphView graphView;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristicProfilesToTransfer;
    private BluetoothGattCharacteristic characteristicProfiles;
    private BluetoothGattCharacteristic characteristicSerialNumber;

    private short currentProfileId;

    private List<BluetoothGattService> listBluetoothGattServices;
    private ArrayList<BluetoothGattDescriptor> arrayListDescriptors;

    private final BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                bluetoothGatt.discoverServices();
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED){

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d("RUN", "services found!");
                listBluetoothGattServices = bluetoothGatt.getServices();

                setProfilesCharacteristic();
                setProfilesToTransferCharacteristic();
                setSerialNumberCharacteristic();

                enableDeviceStatusNotify();
                enableProfileIdNotify();
                enableProfilesNotify();
            } else {
                //error
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS && characteristic.getUuid().toString().equalsIgnoreCase(ProbeGattProfile.CHARACTERISTIC_PROFILES)){
                byte[] rawData = characteristic.getValue();
                String str = new String(rawData);
                Log.d("RUN", "on characteristic read: " + str);
            } else if(status == BluetoothGatt.GATT_SUCCESS && characteristic.getUuid().toString().equalsIgnoreCase(ProbeGattProfile.CHARACTERISTIC_SERIAL_NUMBER)){
                byte[] rawData = characteristic.getValue();
                String str = new String(rawData);
                Log.d("RUN", "on characteristic read: " + str);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d("RUN", "onCharacteristicWrite: SUCCESS");
                if(characteristic.getUuid().toString().equalsIgnoreCase(ProbeGattProfile.CHARACTERISTIC_PROFILES_TO_TRANSFER)){
                    //TODO: sent profile id to probe => how to handle?
                    processProbeData();
                }

            } else {
                Log.d("RUN", "onCharacteristicWrite: FAILURE");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(characteristic.getUuid().toString().equalsIgnoreCase(ProbeGattProfile.CHARACTERISTIC_DEVICE_STATUS)){
                processDeviceStatus(characteristic);
            } else if(characteristic.getUuid().toString().equalsIgnoreCase(ProbeGattProfile.CHARACTERISTIC_PROFILES_ID)){
                processProfileId(characteristic);
            } else if(characteristic.getUuid().toString().equalsIgnoreCase(ProbeGattProfile.CHARACTERISTIC_PROFILES)){
                Log.d("RUN", "NOTIFY! characteristic Profiles");
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d("RUN", "onDescriptorWrite " + descriptor.getCharacteristic().getUuid().toString() );
            enableDescriptor(null);
        }
    };


    private void processProbeData() {
        //these need to be replaced with data from scope
        double[] data = SimulateProbeData.getData();
        String latitude = SimulateProbeData.getLatitude();
        String longitude = SimulateProbeData.getLongitude();
        String time = SimulateProbeData.getTime();
        int depth = SimulateProbeData.getDepth();

        SnowProfile snowProfile = new SnowProfile(currentProfileId, data, latitude, longitude, time, depth);
        snowProfile.writeFile(getApplicationContext());

        //update UI:
        plotData(data);
        //showLocation(); showTime(); ..

    }

    private void plotData(double[] data) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for(int i = 0; i < data.length; i++){
            DataPoint dataPoint = new DataPoint(i, data[i]);
            series.appendData(dataPoint, true, data.length);
        }

        graphView.removeAllSeries();
        graphView.addSeries(series);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.setTitle("FAKE DATA");
    }

    private void setSerialNumberCharacteristic() {
        for(BluetoothGattService service : listBluetoothGattServices){
            String serviceUUID = service.getUuid().toString();
            if(serviceUUID.equalsIgnoreCase(ProbeGattProfile.SERVICE_DEVICE_INFO)){
                characteristicSerialNumber = service.getCharacteristic(getUUID(ProbeGattProfile.CHARACTERISTIC_SERIAL_NUMBER));
                Log.d("RUN", "set char serial num");
                return;
            }
        }
    }

    private void setProfilesCharacteristic() {
        for(BluetoothGattService service : listBluetoothGattServices){
            String serviceUUID = service.getUuid().toString();
            if(serviceUUID.equalsIgnoreCase(ProbeGattProfile.SERVICE_SNOW_PROFILE)){
                characteristicProfiles = service.getCharacteristic(getUUID(ProbeGattProfile.CHARACTERISTIC_PROFILES));
                return;
            }
        }
    }

    private void setProfilesToTransferCharacteristic() {
        for(BluetoothGattService service : listBluetoothGattServices){
            String serviceUUID = service.getUuid().toString();
            if(serviceUUID.equalsIgnoreCase(ProbeGattProfile.SERVICE_SNOW_PROFILE)){
                characteristicProfilesToTransfer = service.getCharacteristic(getUUID(ProbeGattProfile.CHARACTERISTIC_PROFILES_TO_TRANSFER));
                return;
            }
        }
    }

    private void enableProfilesNotify() {
        Log.d("RUN", "enableProfilesNotify");
        for(BluetoothGattService service : listBluetoothGattServices){
            String serviceUUID = service.getUuid().toString();
            if(serviceUUID.equalsIgnoreCase(ProbeGattProfile.SERVICE_SNOW_PROFILE)){
                //BluetoothGattCharacteristic characteristic = service.getCharacteristic(getUUID(ProbeGattProfile.CHARACTERISTIC_PROFILES));
                BluetoothGattDescriptor descriptor = characteristicProfiles.getDescriptor(getUUID(ProbeGattProfile.CHARACTERISTIC_CLIENT_CONFIG));
                if (descriptor != null) {
                    enableDescriptor(descriptor);
                }
            }
        }
    }

    private void enableProfileIdNotify() {
        Log.d("RUN", "enableProfileIdNotify");
        for(BluetoothGattService service : listBluetoothGattServices){
            String serviceUUID = service.getUuid().toString();
            if(serviceUUID.equalsIgnoreCase(ProbeGattProfile.SERVICE_SNOW_PROFILE)){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(getUUID(ProbeGattProfile.CHARACTERISTIC_PROFILES_ID));
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(getUUID(ProbeGattProfile.CHARACTERISTIC_CLIENT_CONFIG));
                if (descriptor != null) {
                    enableDescriptor(descriptor);
                }
            }
        }
    }

    private void enableDeviceStatusNotify() {
        Log.d("RUN", "enableStatusNotify()");

        for(BluetoothGattService service : listBluetoothGattServices){
            String serviceUUID = service.getUuid().toString();
            if(serviceUUID.equalsIgnoreCase(ProbeGattProfile.SERVICE_DEVICE_STATUS)){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(getUUID(ProbeGattProfile.CHARACTERISTIC_DEVICE_STATUS));
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(getUUID(ProbeGattProfile.CHARACTERISTIC_CLIENT_CONFIG));
                if (descriptor != null) {
                    enableDescriptor(descriptor);
                }
            }
        }
    }

    private void processDeviceStatus(BluetoothGattCharacteristic characteristic) {
        final byte[] rawData = characteristic.getValue();
        short index = (short) rawData[0];
        Log.d("RUN", Short.toString(index));

        final String[] statuses = {"ALIGN", "PROBING", "PROCESSING", "", "", "", "", "", "READY", "", ""};
        final String status = statuses[index];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewProbeStatus.setText(status);
            }
        });
    }

    private void processProfileId(BluetoothGattCharacteristic characteristic) {
        byte[] rawData = characteristic.getValue();
        short profileID = (short) (((rawData[1] & 0xFF) << 8) | (rawData[0] & 0xFF));
        final String profileIdString = "PROFILE ID: " + Short.toString(profileID);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewProfileId.setText(profileIdString);
            }
        });

        currentProfileId = profileID;

        //write profile id to profiles_to_transfer characteristic
        characteristicProfilesToTransfer.setValue(rawData);
        bluetoothGatt.writeCharacteristic(characteristicProfilesToTransfer);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_active_probe);

        textViewProbeStatus = findViewById(R.id.textViewProbeStatus);
        textViewProfileId = findViewById(R.id.textViewProfileID);
        graphView = findViewById(R.id.graphViewManageData);

//        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        if(bluetoothManager == null){
//            //error
//            finish();
//        }
//
//        bluetoothAdapter = bluetoothManager.getAdapter();
//        if(bluetoothAdapter == null){
//            //error
//            finish();
//        }
//
//        String bluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_DEVICE_ADDRESS);
//        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothDeviceAddress);
//        if(bluetoothDevice == null){
//            //error
//            finish();
//        }
//
//        bluetoothGatt = bluetoothDevice.connectGatt(this, false, callback);
//        arrayListDescriptors = new ArrayList<BluetoothGattDescriptor>();
    }

    @Override
    protected void onResume(){
        super.onResume();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager == null){
            //error
            finish();
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            //error
            finish();
        }

        String bluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_DEVICE_ADDRESS);
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothDeviceAddress);
        if(bluetoothDevice == null){
            //error
            finish();
        }

        bluetoothGatt = bluetoothDevice.connectGatt(this, false, callback);
        arrayListDescriptors = new ArrayList<BluetoothGattDescriptor>();
    }

    private UUID getUUID(String str){
        return UUID.fromString(str);
    }

    private void enableDescriptor(BluetoothGattDescriptor descriptor) {
        if (descriptor != null) {
            arrayListDescriptors.add(descriptor);
        }

        if (arrayListDescriptors.size() == 0) {
            return;
        }

        BluetoothGattDescriptor nextDescriptor = arrayListDescriptors.get(0);

        nextDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.setCharacteristicNotification(nextDescriptor.getCharacteristic(), true);

        String charUUID = nextDescriptor.getCharacteristic().getUuid().toString();

        if (bluetoothGatt.writeDescriptor(nextDescriptor)) {
            Log.d("RUN", "writeDescriptor success! " + charUUID);
            arrayListDescriptors.remove(0);
        } else {
            Log.d("RUN", "writeDescriptor failed! " + charUUID);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
        }
    }
}

