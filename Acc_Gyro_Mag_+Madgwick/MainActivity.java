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


class MadgwickAHRS {
    private static final float GYRO_MEAS_ERROR = (float)(Math.PI * (5.0 / 180.0));
    private static final float BETA = (float)(Math.sqrt(3.0 / 4.0) * GYRO_MEAS_ERROR);
    private float[] q = {1.0f, 0.0f, 0.0f, 0.0f};

    /**
     * Метод обновления фильтра Маджвика.
     *
     * @param wx угловая скорость по оси X (рад/с)
     * @param wy угловая скорость по оси Y (рад/с)
     * @param wz угловая скорость по оси Z (рад/с)
     * @param ax ускорение по оси X (акселерометр)
     * @param ay ускорение по оси Y (акселерометр)
     * @param az ускорение по оси Z (акселерометр)
     * @param dt временной шаг (сек)
     */
    public void update(float wx, float wy, float wz, float ax, float ay, float az, float dt) {
        float norm = (float)Math.sqrt(ax * ax + ay * ay + az * az);
        if (norm == 0f) return;
        ax /= norm;
        ay /= norm;
        az /= norm;

        float halfq0 = 0.5f * q[0];
        float halfq1 = 0.5f * q[1];
        float halfq2 = 0.5f * q[2];
        float halfq3 = 0.5f * q[3];
        float twoq0 = 2.0f * q[0];
        float twoq1 = 2.0f * q[1];
        float twoq2 = 2.0f * q[2];

        float f1 = twoq1 * q[3] - twoq0 * q[2] - ax;
        float f2 = twoq0 * q[1] + twoq2 * q[3] - ay;
        float f3 = 1.0f - twoq1 * q[1] - twoq2 * q[2] - az;

        float J11or24 = twoq2;
        float J12or23 = 2.0f * q[3];
        float J13or22 = twoq0;
        float J14or21 = twoq1;
        float J32 = 2.0f * J14or21;
        float J33 = 2.0f * J11or24;

        float SEqHatDot0 = J14or21 * f2 - J11or24 * f1;
        float SEqHatDot1 = J12or23 * f1 + J13or22 * f2 - J32 * f3;
        float SEqHatDot2 = J12or23 * f2 - J33 * f3 - J13or22 * f1;
        float SEqHatDot3 = J14or21 * f1 + J11or24 * f2;

        norm = (float)Math.sqrt(SEqHatDot0 * SEqHatDot0 +
                SEqHatDot1 * SEqHatDot1 +
                SEqHatDot2 * SEqHatDot2 +
                SEqHatDot3 * SEqHatDot3);
        if (norm != 0f) {
            SEqHatDot0 /= norm;
            SEqHatDot1 /= norm;
            SEqHatDot2 /= norm;
            SEqHatDot3 /= norm;
        }

        float qDot0 = -halfq1 * wx - halfq2 * wy - halfq3 * wz;
        float qDot1 = halfq0 * wx + halfq2 * wz - halfq3 * wy;
        float qDot2 = halfq0 * wy - halfq1 * wz + halfq3 * wx;
        float qDot3 = halfq0 * wz + halfq1 * wy - halfq2 * wx;

        q[0] += (qDot0 - BETA * SEqHatDot0) * dt;
        q[1] += (qDot1 - BETA * SEqHatDot1) * dt;
        q[2] += (qDot2 - BETA * SEqHatDot2) * dt;
        q[3] += (qDot3 - BETA * SEqHatDot3) * dt;

        norm = (float)Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        if (norm != 0f) {
            q[0] /= norm;
            q[1] /= norm;
            q[2] /= norm;
            q[3] /= norm;
        }
    }

    /**
     * Преобразование кватерниона в Эйлеровы углы (roll, pitch, yaw) в радианах.
     *
     * @return массив из трёх значений: [roll, pitch, yaw]
     */
    public float[] getEulerAngles() {
        float q0 = q[0], q1 = q[1], q2 = q[2], q3 = q[3];

        float roll = (float)Math.atan2(2.0f * (q0 * q1 + q2 * q3), 1.0f - 2.0f * (q1 * q1 + q2 * q2));
        float pitch = (float)Math.asin(2.0f * (q0 * q2 - q3 * q1));
        float yaw = (float)Math.atan2(2.0f * (q0 * q3 + q1 * q2), 1.0f - 2.0f * (q2 * q2 + q3 * q3));

        return new float[] { roll, pitch, yaw };
    }

    /**
     * Метод для получения текущего кватерниона.
     *
     * @return массив кватерниона [q0, q1, q2, q3]
     */
    public float[] getQuaternion() {
        return q.clone();
    }
}


class MadgwickMagnet {
    private static final float GYRO_MEAS_ERROR = (float) (Math.PI * (5.0 / 180.0));
    private static final float GYRO_MEAS_DRIFT = (float) (Math.PI * (0.2 / 180.0));
    private static final float BETA = (float) (Math.sqrt(3.0 / 4.0) * GYRO_MEAS_ERROR);
    private static final float ZETA = (float) (Math.sqrt(3.0 / 4.0) * GYRO_MEAS_DRIFT);

    private float[] q = {1.0f, 0.0f, 0.0f, 0.0f};
    private float b_x = 1.0f, b_z = 0.0f;
    private float w_bx = 0.0f, w_by = 0.0f, w_bz = 0.0f;

    /**
     * Обновление фильтра Маджвика с использованием данных акселерометра, гироскопа и магнитометра.
     *
     * @param wx угловая скорость по оси X (рад/с)
     * @param wy угловая скорость по оси Y (рад/с)
     * @param wz угловая скорость по оси Z (рад/с)
     * @param ax ускорение по оси X (акселерометр)
     * @param ay ускорение по оси Y (акселерометр)
     * @param az ускорение по оси Z (акселерометр)
     * @param mx магнитное поле по оси X (магнитометр)
     * @param my магнитное поле по оси Y (магнитометр)
     * @param mz магнитное поле по оси Z (магнитометр)
     * @param dt временной шаг (сек)
     */
    public void update(float wx, float wy, float wz,
                       float ax, float ay, float az,
                       float mx, float my, float mz,
                       float dt) {
        float norm = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        if (norm == 0f) return;
        ax /= norm;
        ay /= norm;
        az /= norm;

        norm = (float) Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm == 0f) return;
        mx /= norm;
        my /= norm;
        mz /= norm;

        float q0 = q[0], q1 = q[1], q2 = q[2], q3 = q[3];
        float halfq0 = 0.5f * q0;
        float halfq1 = 0.5f * q1;
        float halfq2 = 0.5f * q2;
        float halfq3 = 0.5f * q3;
        float twoq0 = 2.0f * q0;
        float twoq1 = 2.0f * q1;
        float twoq2 = 2.0f * q2;
        float twoq3 = 2.0f * q3;

        float twob_x = 2.0f * b_x;
        float twob_z = 2.0f * b_z;
        float twob_xq0 = 2.0f * b_x * q0;
        float twob_xq1 = 2.0f * b_x * q1;
        float twob_xq2 = 2.0f * b_x * q2;
        float twob_xq3 = 2.0f * b_x * q3;
        float twob_zq0 = 2.0f * b_z * q0;
        float twob_zq1 = 2.0f * b_z * q1;
        float twob_zq2 = 2.0f * b_z * q2;
        float twob_zq3 = 2.0f * b_z * q3;

        float SEq1SEq2 = q0 * q1;
        float SEq1SEq3 = q0 * q2;
        float SEq1SEq4 = q0 * q3;
        float SEq2SEq3 = q1 * q2;
        float SEq2SEq4 = q1 * q3;
        float SEq3SEq4 = q2 * q3;

        float twomx = 2.0f * mx;
        float twomy = 2.0f * my;
        float twomz = 2.0f * mz;

        float f1 = twoq1 * q3 - twoq0 * q2 - ax;
        float f2 = twoq0 * q1 + twoq2 * q3 - ay;
        float f3 = 1.0f - twoq1 * q1 - twoq2 * q2 - az;
        float f4 = twob_x * (0.5f - q2 * q2 - q3 * q3) + twob_z * (q1 * q3 - q0 * q2) - mx;
        float f5 = twob_x * (q1 * q2 - q0 * q3) + twob_z * (q0 * q1 + q2 * q3) - my;
        float f6 = twob_x * (q0 * q2 + q1 * q3) + twob_z * (0.5f - q1 * q1 - q2 * q2) - mz;
        float J11or24 = twoq2;
        float J12or23 = 2.0f * q3;
        float J13or22 = twoq0;
        float J14or21 = twoq1;
        float J32 = 2.0f * J14or21;
        float J33 = 2.0f * J11or24;

        float J41 = twob_zq2;
        float J42 = twob_zq3;
        float J43 = 2.0f * twob_xq2 + twob_zq0;
        float J44 = 2.0f * twob_xq3 - twob_zq1;

        float J51 = twob_xq3 - twob_zq1;
        float J52 = twob_xq2 + twob_zq0;
        float J53 = twob_xq1 + twob_zq3;
        float J54 = twob_xq0 - twob_zq2;

        float J61 = twob_xq2;
        float J62 = twob_xq3 - 2.0f * twob_zq1;
        float J63 = twob_xq0 - 2.0f * twob_zq2;
        float J64 = twob_xq1;

        float SEqHatDot0 = J14or21 * f2 - J11or24 * f1 - J41 * f4 - J51 * f5 + J61 * f6;
        float SEqHatDot1 = J12or23 * f1 + J13or22 * f2 - J32 * f3 + J42 * f4 + J52 * f5 + J62 * f6;
        float SEqHatDot2 = J12or23 * f2 - J33 * f3 - J13or22 * f1 - J43 * f4 + J53 * f5 + J63 * f6;
        float SEqHatDot3 = J14or21 * f1 + J11or24 * f2 - J44 * f4 - J54 * f5 + J64 * f6;

        norm = (float) Math.sqrt(SEqHatDot0 * SEqHatDot0 +
                SEqHatDot1 * SEqHatDot1 +
                SEqHatDot2 * SEqHatDot2 +
                SEqHatDot3 * SEqHatDot3);
        if (norm != 0f) {
            SEqHatDot0 /= norm;
            SEqHatDot1 /= norm;
            SEqHatDot2 /= norm;
            SEqHatDot3 /= norm;
        }

        float w_err_x = twoq0 * SEqHatDot1 - twoq1 * SEqHatDot0 - twoq2 * SEqHatDot3 + twoq3 * SEqHatDot2;
        float w_err_y = twoq0 * SEqHatDot2 + twoq1 * SEqHatDot3 - twoq2 * SEqHatDot0 - twoq3 * SEqHatDot1;
        float w_err_z = twoq0 * SEqHatDot3 - twoq1 * SEqHatDot2 + twoq2 * SEqHatDot1 - twoq3 * SEqHatDot0;

        w_bx += w_err_x * dt * ZETA;
        w_by += w_err_y * dt * ZETA;
        w_bz += w_err_z * dt * ZETA;
        wx -= w_bx;
        wy -= w_by;
        wz -= w_bz;

        float qDot0 = -halfq1 * wx - halfq2 * wy - halfq3 * wz;
        float qDot1 = halfq0 * wx + halfq2 * wz - halfq3 * wy;
        float qDot2 = halfq0 * wy - halfq1 * wz + halfq3 * wx;
        float qDot3 = halfq0 * wz + halfq1 * wy - halfq2 * wx;

        q0 += (qDot0 - BETA * SEqHatDot0) * dt;
        q1 += (qDot1 - BETA * SEqHatDot1) * dt;
        q2 += (qDot2 - BETA * SEqHatDot2) * dt;
        q3 += (qDot3 - BETA * SEqHatDot3) * dt;

        norm = (float) Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        if (norm != 0f) {
            q0 /= norm;
            q1 /= norm;
            q2 /= norm;
            q3 /= norm;
        }
        q[0] = q0; q[1] = q1; q[2] = q2; q[3] = q3;
        SEq1SEq2 = q0 * q1;
        SEq1SEq3 = q0 * q2;
        SEq1SEq4 = q0 * q3;
        SEq2SEq3 = q1 * q2;
        SEq2SEq4 = q1 * q3;
        SEq3SEq4 = q2 * q3;
        float h_x = twomx * (0.5f - q2 * q2 - q3 * q3) + twomy * (q1 * q2 - q0 * q3) + twomz * (q1 * q3 + q0 * q2);
        float h_y = twomx * (q1 * q2 + q0 * q3) + twomy * (0.5f - q1 * q1 - q3 * q3) + twomz * (q2 * q3 - q0 * q1);
        float h_z = twomx * (q1 * q3 - q0 * q2) + twomy * (q2 * q3 + q0 * q1) + twomz * (0.5f - q1 * q1 - q2 * q2);
        b_x = (float) Math.sqrt(h_x * h_x + h_y * h_y);
        b_z = h_z;
    }

    /**
     * Преобразование текущего кватерниона в Эйлеровы углы (roll, pitch, yaw) в радианах.
     *
     * @return массив из трёх значений: [roll, pitch, yaw]
     */
    public float[] getEulerAngles() {
        float q0 = q[0], q1 = q[1], q2 = q[2], q3 = q[3];
        float roll = (float) Math.atan2(2.0f * (q0 * q1 + q2 * q3),
                1.0f - 2.0f * (q1 * q1 + q2 * q2));
        float pitch = (float) Math.asin(2.0f * (q0 * q2 - q3 * q1));
        float yaw = (float) Math.atan2(2.0f * (q0 * q3 + q1 * q2),
                1.0f - 2.0f * (q2 * q2 + q3 * q3));
        return new float[]{roll, pitch, yaw};
    }

    /**
     * Получение текущего кватерниона.
     *
     * @return массив кватерниона [q0, q1, q2, q3]
     */
    public float[] getQuaternion() {
        return q.clone();
    }
}


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

    private MadgwickAHRS madgwickFilter = new MadgwickAHRS();
    private MadgwickMagnet madgwickFilterMagnet = new MadgwickMagnet();


    private TextView sensorListTextView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация файла для записи данных
        try {
            File dir = getFilesDir();
            if (dir != null) {
                dataFile = new File(dir, "sensor_data.csv"); // Имя файла
                fileWriter = new FileWriter(dataFile, false); // Открываем файл (false - перезаписать)
                fileWriter.write("Time,X_Acc,Y_Acc,Z_Acc,X_Gyro,Y_Gyro,Z_Gyro,X_Mag,Y_Mag,Z_Mag\n"); // Заголовки столбцов
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
        

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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

            //xAcceleration.setText(String.format("%.2f", xAcc));
            //yAcceleration.setText(String.format("%.2f", yAcc));
            //zAcceleration.setText(String.format("%.2f", zAcc));


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
            axlineChart.invalidate();

            axlineChart.notifyDataSetChanged();
            axlineChart.invalidate();
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            xGyros = event.values[0];
            yGyros = event.values[1];
            zGyros = event.values[2];

//            xGyroTextField.setText(String.format("%.2f", xGyros));
//            yGyroTextField.setText(String.format("%.2f", yGyros));
//            zGyroTextField.setText(String.format("%.2f", zGyros));


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
            gylineChart.invalidate();

            gylineChart.notifyDataSetChanged();
            gylineChart.invalidate();
            
        }





        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            xMag = event.values[0];
            yMag = event.values[1];
            zMag = event.values[2];

//            xMagnet.setText(String.format("%.2f", xMag));
//            yMagnet.setText(String.format("%.2f", yMag));
//            zMagnet.setText(String.format("%.2f", zMag));


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

        xGyroTextField.setText(String.valueOf(accelDataReady));
        yGyroTextField.setText(String.valueOf(gyroDataReady));
        zGyroTextField.setText(String.valueOf(magnetDataReady));

        if (gyroDataReady) {
            madgwickFilter.update(xGyros, yGyros, zGyros, xAcc, yAcc, zAcc, dt);

            float[] angles = madgwickFilter.getEulerAngles();
            float rollDeg = (float)Math.toDegrees(angles[0]);
            float pitchDeg = (float)Math.toDegrees(angles[1]);
            float yawDeg = (float)Math.toDegrees(angles[2]);

            xAcceleration.setText(String.format("%.2f", rollDeg));
            yAcceleration.setText(String.format("%.2f", pitchDeg));
            zAcceleration.setText(String.format("%.2f", yawDeg));

            if (magnetDataReady) {
                madgwickFilterMagnet.update(xGyros, yGyros, zGyros, xAcc, yAcc, zAcc, xMag, yMag, zMag, dt);

                float[] angles1 = madgwickFilterMagnet.getEulerAngles();
                float rollDeg1 = (float)Math.toDegrees(angles1[0]);
                float pitchDeg1 = (float)Math.toDegrees(angles1[1]);
                float yawDeg1 = (float)Math.toDegrees(angles1[2]);



                xMagnet.setText(String.format("%.2f", rollDeg1));
                yMagnet.setText(String.format("%.2f", pitchDeg1));
                zMagnet.setText(String.format("%.2f", yawDeg1));
                magnetDataReady = false;
            }

            gyroDataReady = false;


            previousTimeMillis = currentTimeMillis;
        }


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
