package com.example.avatechsnowscope;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

/*
TODO:
X update ListView to show custom view with id, date, and graph
- make entire custom row clickable
- keep track of items selected?
- update view of selected items?
- SHARE WITH TEAM.
 */

public class ManageProbeDataActivity extends AppCompatActivity {

    private ArrayList<String> arrayListFiles;
    //private ArrayAdapter<String> arrayAdapter;
    private ListView listView;
    private CustomAdapter arrayAdapter;

    public static final String EXTRA_FILENAME = "fileName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_probe_data);
        Log.d("RUN", "onCreate: ManageProbeActivity");

        listView = findViewById(R.id.listViewFiles);
        arrayListFiles = new ArrayList<String>();
        //arrayListFiles.add("3_19_19,_2_36_18_PM_MDT_6455.txt");

        //arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, arrayListFiles);
        arrayAdapter = new CustomAdapter(getApplicationContext(), R.layout.item_custom, arrayListFiles);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileName = arrayListFiles.get(i);
                Intent intent = new Intent(getApplicationContext(), ViewProbeDataActivity.class);
                intent.putExtra(EXTRA_FILENAME, fileName);
                startActivity(intent);
            }
        });

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                Log.d("RUN", "stat change " + Integer.toString(i) + " " + Boolean.toString(b));
                if(b){
                    ShapeDrawable shape = new ShapeDrawable(new RectShape());
                    shape.getPaint().setColor(getResources().getColor(R.color.colorAccent));
                    shape.getPaint().setStyle(Paint.Style.STROKE);
                    shape.getPaint().setStrokeWidth(8);
                    listView.getChildAt(i).setBackground(shape);
                } else {
                    listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Log.d("RUN", menuItem.toString());

                ArrayList<String> selectedFiles = new ArrayList<String>();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                for(int i = 0; i < checked.size(); i++){
                    if(checked.valueAt(i)){
                        int index = checked.keyAt(i);
                        String fileName = arrayAdapter.getItem(index);
                        listView.setItemChecked(index, false);
                        selectedFiles.add(fileName);
                        Log.d("RUN", (String) arrayAdapter.getItem(index));
                    }
                }

                switch (menuItem.getItemId()){
                    case R.id.menuItemDelete:
                        for(String fileName : selectedFiles){
                            Log.d("RUN", "delete: " + fileName);
                            getApplicationContext().deleteFile(fileName);
                            arrayListFiles.remove(fileName);
                            arrayAdapter.notifyDataSetChanged();
                        }

                        return true;

                    case R.id.menuItemShare:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Files to send");
                        intent.setType("text/plain");
                        ArrayList<Uri> filesToSend = new ArrayList<Uri>();
                        for(String fileName : selectedFiles /* List of the files you want to send */) {
                            File path = getFilesDir();
                            File file = new File(path, fileName);
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.example.avatechsnowscope.fileprovider", file);
                            filesToSend.add(uri);
                        }

                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToSend);
                        startActivity(intent);
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        arrayListFiles.clear();

        File[] files = getApplicationContext().getFilesDir().listFiles();
        for(File file : files){
            String fileName = file.getName();
            if(fileName.contains(".txt")) {
                arrayListFiles.add(file.getName());
            }
        }
        arrayAdapter.notifyDataSetChanged();
    }

    public class CustomAdapter extends ArrayAdapter<String>{
        private ArrayList<String> arrayList;
        private Context context;
        private int resource;

        public CustomAdapter(Context context, int resource, ArrayList<String> arrayList) {
            super(context, resource);
            Log.d("RUN", "CustomAdapter()");

            this.context = context;
            this.arrayList = arrayList;
            this.resource = resource;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public String getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("RUN", "getView");

            String fileName = arrayList.get(position);
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            }

            SnowProfile snowProfile = new SnowProfile();
            snowProfile.readFile(getApplicationContext(), fileName);

            TextView textViewProfileId = convertView.findViewById(R.id.itemCustomTextViewProfileId);
            TextView textViewFilename = convertView.findViewById(R.id.itemCustomTextViewFilename);
            GraphView graphView = convertView.findViewById(R.id.itemCustomGraphView);

            textViewProfileId.setText(Short.toString(snowProfile.getId()));
            textViewFilename.setText(fileName);
            plotData(snowProfile.getData(), graphView);
            return convertView;
        }

        private void plotData(double[] data, GraphView graphView) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            for(int i = 0; i < data.length; i++){
                DataPoint dataPoint = new DataPoint(i, data[i]);
                series.appendData(dataPoint, true, data.length);
            }

            graphView.removeAllSeries();
            graphView.addSeries(series);
            graphView.getViewport().setXAxisBoundsManual(true);
            graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graphView.setTitle("FAKE DATA");
        }
    }
}
