package com.example.avatechsnowscope;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class SnowProfile {

    private short id;
    private double[] data;
    private String latitude;
    private String longitude;
    private String time;
    private int depth;

    public SnowProfile(){}

    public SnowProfile(short id, double[] data, String latitude, String longitude, String time, int depth) {
        this.id = id;
        this.data = data;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.depth = depth;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public void writeFile(Context context) {

        String fmtTime = time.replaceAll(" ", "_");
        fmtTime = fmtTime.replaceAll(":", "_");
        fmtTime = fmtTime.replaceAll("/", "_");
        //String filename = Short.toString(id) + "_" + fmtTime + ".txt";
        String filename = fmtTime + "_" + Short.toString(id) + ".txt";

        Log.d("RUN", "writeFile " + filename);

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            bufferedWriter.write("id: " + Short.toString(id) + "\n");
            bufferedWriter.write("latitude: " + latitude + "\n");
            bufferedWriter.write("longitude: " + longitude + "\n");
            bufferedWriter.write("time: " + time + "\n");
            bufferedWriter.write("depth: " + Integer.toString(depth) + "\n");
            bufferedWriter.write("data: \n");
            for(int i = 0; i < depth; i++){
                String dataStr = Double.toString(data[i]);
                bufferedWriter.write(dataStr + "\n");
            }

            bufferedWriter.close();
            Log.d("RUN", "wrote to file " + filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFile(Context context, String fileName) {
        Log.d("RUN", "readFile " + fileName);
        try {

            FileInputStream fileInputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            String[] words;

            //"id: " + Short.toString(id) + "\n"
            line = bufferedReader.readLine();
            id = getShortToken(line);

            line = bufferedReader.readLine();
            latitude = getStringToken(line);

            line = bufferedReader.readLine();
            longitude = getStringToken(line);

            line = bufferedReader.readLine();
            time = getStringToken(line);

            line = bufferedReader.readLine();
            depth = getIntToken(line);

            //read the "data: " line
            line = bufferedReader.readLine();
            data = new double[this.depth];
            for(int i = 0; i < this.depth; i++){
                line = bufferedReader.readLine();
                data[i] = Double.parseDouble(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStringToken(String line){
        String[] words = line.split(" ");
        return words[1];
    }

    private short getShortToken(String line){
        String[] words = line.split(" ");
        return Short.parseShort(words[1]);
    }

    private int getIntToken(String line){
        String[] words = line.split(" ");
        return Integer.parseInt(words[1]);
    }

}
