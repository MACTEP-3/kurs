package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.LinkedList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer, sensorGyroscope, sensorRotationVector, sensorgameRotationVector, gravity;
    private TextView xGyroTextField, yGyroTextField, zGyroTextField, xAcceleration, yAcceleration, zAcceleration, xRotationVector, yRotationVector, zRotationVector, xGameRotationVector, yGameRotationVector, zGameRotationVector, xGravity, yGravity, zGravity;
    private LineChart axlineChart, gylineChart, rvlineChart;
    private LinkedList<Entry> axxyData = new LinkedList<>();
    private LinkedList<Entry> axxzData = new LinkedList<>();
    private LinkedList<Entry> axzyData = new LinkedList<>();
    private LinkedList<Entry> gyxyData = new LinkedList<>();
    private LinkedList<Entry> gyxzData = new LinkedList<>();
    private LinkedList<Entry> gyzyData = new LinkedList<>();
    private LinkedList<Entry> rvxyData = new LinkedList<>();
    private LinkedList<Entry> rvxzData = new LinkedList<>();
    private LinkedList<Entry> rvzyData = new LinkedList<>();
    private long startTime;
    private File dataFile;
    private FileWriter fileWriter;
    private float xAcc = 0, yAcc = 0, zAcc = 0;
    private float xGyros = 0, yGyros = 0, zGyros = 0;
    private float xRV = 0, yRV = 0, zRV = 0;

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
        xRotationVector = findViewById(R.id.xRotationVector);
        yRotationVector = findViewById(R.id.yRotationVector);
        zRotationVector = findViewById(R.id.zRotationVector);
        

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

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorRotationVector, SensorManager.SENSOR_DELAY_FASTEST);

        axlineChart = findViewById(R.id.axlineChart);
        gylineChart = findViewById(R.id.gylineChart);
        rvlineChart = findViewById(R.id.rvlineChart);
        startTime = System.currentTimeMillis();

        axlineChart.setDragEnabled(true);
        axlineChart.setScaleEnabled(true);
        axlineChart.getDescription().setEnabled(false);
        axlineChart.setData(new LineData(new LineDataSet(new ArrayList<>(), "XY Accelerometr")));
        gylineChart.setDragEnabled(true);
        gylineChart.setScaleEnabled(true);
        gylineChart.getDescription().setEnabled(false);
        gylineChart.setData(new LineData(new LineDataSet(new ArrayList<>(), "XY Gyroscope")));
        rvlineChart.setDragEnabled(true);
        rvlineChart.setScaleEnabled(true);
        rvlineChart.getDescription().setEnabled(false);
        rvlineChart.setData(new LineData(new LineDataSet(new ArrayList<>(), "XY RotationVector")));
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

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            xRV = event.values[0];
            yRV = event.values[1];
            zRV = event.values[2];

            xRotationVector.setText(String.format("%.2f", xRV));
            yRotationVector.setText(String.format("%.2f", yRV));
            zRotationVector.setText(String.format("%.2f", zRV));


            magnetDataReady = true;

            if (rvxyData.size() > 100) {
                rvxyData.remove(0);
            }
            if (rvxzData.size() > 100) {
                rvxzData.remove(0);
            }
            if (rvzyData.size() > 100) {
                rvzyData.remove(0);
            }

            rvxyData.add(new Entry(timeInSeconds, xRV));
            rvxzData.add(new Entry(timeInSeconds, yRV));
            rvzyData.add(new Entry(timeInSeconds, zRV));

            LineDataSet rvxyDataSet = new LineDataSet(rvxyData, "XY Acceleration");
            rvxyDataSet.setLineWidth(2f);
            rvxyDataSet.setColor(0xFFFF0000);
            rvxyDataSet.setCircleRadius(1f);
            LineDataSet rvxzDataSet = new LineDataSet(rvxzData, "XZ Acceleration");
            rvxzDataSet.setLineWidth(2f);
            rvxzDataSet.setColor(0xFF00FF00);
            rvxzDataSet.setCircleRadius(1f);
            LineDataSet rvzyDataSet = new LineDataSet(rvzyData, "ZY Acceleration");
            rvzyDataSet.setLineWidth(2f);
            rvzyDataSet.setColor(0xFF0000FF);
            rvzyDataSet.setCircleRadius(1f);

            LineData gylineData = new LineData(rvxyDataSet, rvxzDataSet, rvzyDataSet);
            rvlineChart.setData(gylineData);
            rvlineChart.invalidate();

            rvlineChart.notifyDataSetChanged();
            rvlineChart.invalidate();

        }

//        xGyroTextField.setText(String.valueOf(accelDataReady));
//        yGyroTextField.setText(String.valueOf(gyroDataReady));
//        zGyroTextField.setText(String.valueOf(magnetDataReady));


        try {
            String dataLine = "'" + timeInSeconds + "',"
                    + xAcc + "," + yAcc + "," + zAcc + ","
                    + xGyros + "," + yGyros + "," + zGyros + ","
                    + xRV + "," + yRV + "," + zRV + "\n";

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
