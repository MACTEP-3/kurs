package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.LinkedList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Environment;


import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, magnetometr;
    private TextView xGyroTextField, yGyroTextField, zGyroTextField, xAcceleration, yAcceleration, zAcceleration, xMagnet, yMagnet, zMagnet;
    private LineChart axlineChart, gylineChart, malineChart;
    private LinkedList<Entry> axxyData = new LinkedList<>();
    private LinkedList<Entry> axxzData = new LinkedList<>();
    private LinkedList<Entry> axzyData = new LinkedList<>();
    private LinkedList<Entry> gyxyData = new LinkedList<>();
    private LinkedList<Entry> gyxzData = new LinkedList<>();
    private LinkedList<Entry> gyzyData = new LinkedList<>();
    private LinkedList<Entry> maxyData = new LinkedList<>();
    private LinkedList<Entry> maxzData = new LinkedList<>();
    private LinkedList<Entry> mazyData = new LinkedList<>();
    private long startTime;
    private File dataFile;
    private FileWriter fileWriter;
    private float xAcc = 0, yAcc = 0, zAcc = 0;
    private float xGyros = 0, yGyros = 0, zGyros = 0;
    private float xMag = 0, yMag = 0, zMag = 0;

    private boolean accelDataReady = false;
    private boolean gyroDataReady = false;
    private boolean magnetDataReady = false;
    private long previousTimeMillis = 0;

    private TextView sensorListTextView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            File dir = getFilesDir();
            if (dir != null) {
                dataFile = new File(dir, "sensor_data.csv"); 
                fileWriter = new FileWriter(dataFile, false); 
                fileWriter.write("Time,X_Acc,Y_Acc,Z_Acc,X_Gyro,Y_Gyro,Z_Gyro,X_Mag,Y_Mag,Z_Mag\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        xGyroTextField = findViewById(R.id.xGyroscopeXML);
        yGyroTextField = findViewById(R.id.yGyroscopeXML);
        zGyroTextField = findViewById(R.id.zGyroscopeXML);
        xAcceleration = findViewById(R.id.xAcceleration);
        yAcceleration = findViewById(R.id.yAcceleration);
        zAcceleration = findViewById(R.id.zAcceleration);
        xMagnet = findViewById(R.id.xMagnetometr);
        yMagnet = findViewById(R.id.yMagnetometr);
        zMagnet = findViewById(R.id.zMagnetometr);
        

        // Инициализация датчиков
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Для получения списка всех датчиков
        sensorListTextView = findViewById(R.id.sensorListTextView);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        StringBuilder sensorInfo = new StringBuilder("Доступные датчики:\n");
        for (Sensor sensor : sensorList) {
            sensorInfo.append("Название: ").append(sensor.getName()).append("\n");
            sensorInfo.append("Тип: ").append(sensor.getType()).append("\n");
            sensorInfo.append("Модель: ").append(sensor.getVendor()).append("\n");
            sensorInfo.append("Версия: ").append(sensor.getVersion()).append("\n");
            sensorInfo.append("-------------------------\n");
        }

        sensorListTextView.setText(sensorInfo.toString());

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometr = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometr, SensorManager.SENSOR_DELAY_NORMAL);

        axlineChart = findViewById(R.id.axlineChart);
        gylineChart = findViewById(R.id.gylineChart);
        malineChart = findViewById(R.id.malineChart);
        startTime = System.currentTimeMillis();

        axlineChart.setDragEnabled(true);
        axlineChart.setScaleEnabled(true);
        axlineChart.getDescription().setEnabled(false);
        axlineChart.setData(new LineData(new LineDataSet(new ArrayList<>(), "XY Acceleration")));
        gylineChart.setDragEnabled(true);
        gylineChart.setScaleEnabled(true);
        gylineChart.getDescription().setEnabled(false);
        gylineChart.setData(new LineData(new LineDataSet(new ArrayList<>(), "XY Acceleration")));
        malineChart.setDragEnabled(true);
        malineChart.setScaleEnabled(true);
        malineChart.getDescription().setEnabled(false);
        malineChart.setData(new LineData(new LineDataSet(new ArrayList<>(), "XY Acceleration")));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float timeInSeconds = (System.currentTimeMillis() - startTime) / 1000f;

        long currentTimeMillis = System.currentTimeMillis();


        float dt = (currentTimeMillis - previousTimeMillis) / 1000f;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xAcc = event.values[0];
            yAcc = event.values[1];
            zAcc = event.values[2];

            xAcceleration.setText(String.format("%.2f", xAcc));
            yAcceleration.setText(String.format("%.2f", yAcc));
            zAcceleration.setText(String.format("%.2f", zAcc));


            accelDataReady = true;

            if (axxyData.size() > 100) {
                axxyData.remove(0);
            }
            if (axxzData.size() > 100) {
                axxzData.remove(0);
            }
            if (axzyData.size() > 100) {
                axzyData.remove(0);
            }

            axxyData.add(new Entry(timeInSeconds, xAcc));
            axxzData.add(new Entry(timeInSeconds, yAcc));
            axzyData.add(new Entry(timeInSeconds, zAcc));

            LineDataSet axxyDataSet = new LineDataSet(axxyData, "XY Acceleration");
            axxyDataSet.setLineWidth(2f);
            axxyDataSet.setColor(0xFFFF0000);
            axxyDataSet.setCircleRadius(1f);
            LineDataSet axxzDataSet = new LineDataSet(axxzData, "XZ Acceleration");
            axxzDataSet.setLineWidth(2f);
            axxzDataSet.setColor(0xFF00FF00);
            axxzDataSet.setCircleRadius(1f);
            LineDataSet axzyDataSet = new LineDataSet(axzyData, "ZY Acceleration");
            axzyDataSet.setLineWidth(2f);
            axzyDataSet.setColor(0xFF0000FF);
            axzyDataSet.setCircleRadius(1f);

            LineData axlineData = new LineData(axxyDataSet, axxzDataSet, axzyDataSet);
            axlineChart.setData(axlineData);
            axlineChart.invalidate(); // Перерисовка графика

            axlineChart.notifyDataSetChanged();
            axlineChart.invalidate();
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            xGyros = event.values[0];
            yGyros = event.values[1];
            zGyros = event.values[2];

            xGyroTextField.setText(String.format("%.2f", xGyros));
            yGyroTextField.setText(String.format("%.2f", yGyros));
            zGyroTextField.setText(String.format("%.2f", zGyros));


            gyroDataReady = true;

            if (gyxyData.size() > 100) {
                gyxyData.remove(0);
            }
            if (gyxzData.size() > 100) {
                gyxzData.remove(0);
            }
            if (gyzyData.size() > 100) {
                gyzyData.remove(0);
            }

            gyxyData.add(new Entry(timeInSeconds, xGyros));
            gyxzData.add(new Entry(timeInSeconds, yGyros));
            gyzyData.add(new Entry(timeInSeconds, zGyros));

            LineDataSet gyxyDataSet = new LineDataSet(gyxyData, "XY Acceleration");
            gyxyDataSet.setLineWidth(2f);
            gyxyDataSet.setColor(0xFFFF0000);
            gyxyDataSet.setCircleRadius(1f);
            LineDataSet gyxzDataSet = new LineDataSet(gyxzData, "XZ Acceleration");
            gyxzDataSet.setLineWidth(2f);
            gyxzDataSet.setColor(0xFF00FF00);
            gyxzDataSet.setCircleRadius(1f);
            LineDataSet gyzyDataSet = new LineDataSet(gyzyData, "ZY Acceleration");
            gyzyDataSet.setLineWidth(2f);
            gyzyDataSet.setColor(0xFF0000FF);
            gyzyDataSet.setCircleRadius(1f);

            LineData gylineData = new LineData(gyxyDataSet, gyxzDataSet, gyzyDataSet);
            gylineChart.setData(gylineData);
            gylineChart.invalidate(); // Перерисовка графика

            gylineChart.notifyDataSetChanged();
            gylineChart.invalidate();
            
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            xMag = event.values[0];
            yMag = event.values[1];
            zMag = event.values[2];

            xMagnet.setText(String.format("%.2f", xMag));
            yMagnet.setText(String.format("%.2f", yMag));
            zMagnet.setText(String.format("%.2f", zMag));


            magnetDataReady = true;

            if (maxyData.size() > 100) {
                maxyData.remove(0);
            }
            if (maxzData.size() > 100) {
                maxzData.remove(0);
            }
            if (mazyData.size() > 100) {
                mazyData.remove(0);
            }

            maxyData.add(new Entry(timeInSeconds, xMag));
            maxzData.add(new Entry(timeInSeconds, yMag));
            mazyData.add(new Entry(timeInSeconds, zMag));

            LineDataSet gyxyDataSet = new LineDataSet(maxyData, "XY Acceleration");
            gyxyDataSet.setLineWidth(2f);
            gyxyDataSet.setColor(0xFFFF0000);
            gyxyDataSet.setCircleRadius(1f);
            LineDataSet gyxzDataSet = new LineDataSet(maxzData, "XZ Acceleration");
            gyxzDataSet.setLineWidth(2f);
            gyxzDataSet.setColor(0xFF00FF00);
            gyxzDataSet.setCircleRadius(1f);
            LineDataSet gyzyDataSet = new LineDataSet(mazyData, "ZY Acceleration");
            gyzyDataSet.setLineWidth(2f);
            gyzyDataSet.setColor(0xFF0000FF);
            gyzyDataSet.setCircleRadius(1f);

            LineData gylineData = new LineData(gyxyDataSet, gyxzDataSet, gyzyDataSet);
            malineChart.setData(gylineData);
            malineChart.invalidate(); 

            malineChart.notifyDataSetChanged();
            malineChart.invalidate();

        }

//        xGyroTextField.setText(String.valueOf(accelDataReady));
//        yGyroTextField.setText(String.valueOf(gyroDataReady));
//        zGyroTextField.setText(String.valueOf(magnetDataReady));


        try {
            String dataLine = "'" + timeInSeconds + "',"
                    + xAcc + "," + yAcc + "," + zAcc + ","
                    + xGyros + "," + yGyros + "," + zGyros + ","
                    + xMag + "," + yMag + "," + zMag + "\n";

            fileWriter.write(dataLine);

            accelDataReady = false;
            gyroDataReady = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sensorManager.unregisterListener(this);
    }
}
