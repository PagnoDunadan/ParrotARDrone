package pero.fesb.hr.parrotardrone;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


public class Orijentacija extends Activity implements SensorEventListener {

    //  Listener
    public interface Listener {
        void onOrientationChanged(float pitch, float roll);
    }

    private final WindowManager mWindowManager;
    private final SensorManager mSensorManager;

    @Nullable
    private final Sensor mRotationSensor;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private int mLastAccuracy;
    private Listener mListener;
    private static final int SENSOR_DELAY_MICROS = 50 * 1000; // 50ms

    float[] mGravity;
    float[] mGeomagnetic;
    static float azimut;

    // KONSTRUKTOR
    public Orijentacija(AppCompatActivity activity) {

        // INIT manageri
        mWindowManager = activity.getWindow().getWindowManager();
        mSensorManager = (SensorManager) activity.getSystemService(AppCompatActivity.SENSOR_SERVICE);

        // INIT senzori
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }



    // Metoda za start Listenera
    public void startListening(Listener listener) {
        if (mListener == listener) {
            return;
        }
        mListener = listener;
        if (mRotationSensor == null) {
            return;
        }
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS);
    }

    // Metoda za stop Listenera
    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {



        if (mListener == null)  return;
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)    return;

        // ziroskop
        if (event.sensor == mRotationSensor)    updateOrientation(event.values);
        // akcelerometar
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)    mGravity = event.values;
        // magnetometar
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)   mGeomagnetic = event.values;

        /*  Ovo se dade iskoristit da bi se kombiniralo sa akcelerometrom

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation contains: azimut, pitch and roll
            }
        } */

    }



    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Remap the axes as if the device screen was the instrument panel,
        // and adjust the rotation matrix for the device orientation.
        switch (mWindowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }

        // ovo čudo dobije rotacijsku matricu u odnosu na gravitacijski vektor
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);

        float[] rotacijska90 = new float[9];
        float[] rez = new float[9];
        double kut = Math.PI/2;

        // rotacijska matrica koja rotira po Xu za 90
        rotacijska90[0] = 1;
        rotacijska90[1] = 0;
        rotacijska90[2] = 0;
        rotacijska90[3] = 0;
        rotacijska90[4] = (float)Math.cos(kut);
        rotacijska90[5] = -1f * (float)Math.sin(kut);
        rotacijska90[6] = 0;
        rotacijska90[7] = (float)Math.sin(kut);
        rotacijska90[8] = (float)Math.cos(kut);

        // hardkor množenje matrica
        rez[0] = adjustedRotationMatrix[0] * rotacijska90[0] + adjustedRotationMatrix[1] * rotacijska90[3] + adjustedRotationMatrix[2] * rotacijska90[6];
        rez[1] = adjustedRotationMatrix[0] * rotacijska90[1] + adjustedRotationMatrix[1] * rotacijska90[4] + adjustedRotationMatrix[2] * rotacijska90[7];
        rez[2] = adjustedRotationMatrix[0] * rotacijska90[2] + adjustedRotationMatrix[1] * rotacijska90[5] + adjustedRotationMatrix[2] * rotacijska90[8];
        rez[3] = adjustedRotationMatrix[3] * rotacijska90[0] + adjustedRotationMatrix[4] * rotacijska90[3] + adjustedRotationMatrix[5] * rotacijska90[6];
        rez[4] = adjustedRotationMatrix[3] * rotacijska90[1] + adjustedRotationMatrix[4] * rotacijska90[4] + adjustedRotationMatrix[5] * rotacijska90[7];
        rez[5] = adjustedRotationMatrix[3] * rotacijska90[2] + adjustedRotationMatrix[4] * rotacijska90[5] + adjustedRotationMatrix[5] * rotacijska90[8];
        rez[6] = adjustedRotationMatrix[6] * rotacijska90[0] + adjustedRotationMatrix[7] * rotacijska90[3] + adjustedRotationMatrix[8] * rotacijska90[6];
        rez[7] = adjustedRotationMatrix[6] * rotacijska90[1] + adjustedRotationMatrix[7] * rotacijska90[4] + adjustedRotationMatrix[8] * rotacijska90[7];
        rez[8] = adjustedRotationMatrix[6] * rotacijska90[2] + adjustedRotationMatrix[7] * rotacijska90[5] + adjustedRotationMatrix[8] * rotacijska90[8];
        adjustedRotationMatrix = rez;


        // Transform rotation matrix into azimuth/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        // Pretvara iz radijana u stupnjeve
        float pitch = orientation[1] * -57;
        float roll = orientation[2] * -57;
        // float azimut = orientation[0] * -57;

        mListener.onOrientationChanged(pitch, roll);
    }











    /*  Ovo se moze iskoristiti za akcelerometar

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    */
}
