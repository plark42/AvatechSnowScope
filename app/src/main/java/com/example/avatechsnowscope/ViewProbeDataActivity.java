package com.example.avatechsnowscope;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;

public class ViewProbeDataActivity extends AppCompatActivity {

    private String fileName;
    private SnowProfile snowProfile = new SnowProfile();
    private TextView textViewProfileId;
    private GraphView graphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_probe_data);

        textViewProfileId = findViewById(R.id.textViewProfileId);
        graphView = findViewById(R.id.graphViewManageData);

        fileName = getIntent().getStringExtra(ManageProbeDataActivity.EXTRA_FILENAME);
        Log.d("RUN", fileName);

        //snowProfile = new SnowProfile();
        snowProfile.readFile(getApplicationContext(), fileName);
        if(snowProfile == null){
            Log.d("RUN", "it's null!");

        }
        Log.d("RUN", "here");
        displayProfileId();
        plotData(snowProfile.getData());
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

    private void displayProfileId() {
        textViewProfileId.setText("PROFILE ID: " + Short.toString(snowProfile.getId()));
    }

    public void onClickShare(View view) {
        Toast.makeText(getApplicationContext(), "share: " + fileName, Toast.LENGTH_SHORT).show();
        File path = getFilesDir();
        File file = new File(path, fileName);
        Uri uri = FileProvider.getUriForFile(this, "com.example.avatechsnowscope.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    public void onClickDelete(View view) {
        Toast.makeText(getApplicationContext(), "delete: " + fileName, Toast.LENGTH_SHORT).show();
        getApplicationContext().deleteFile(fileName);
        finish();
    }
}
