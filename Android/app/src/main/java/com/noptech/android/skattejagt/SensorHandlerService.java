package com.noptech.android.skattejagt;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SensorHandlerService implements SensorEventListener, LocationListener {

    private static final String TAG = "SensorHandlerService";
    MainActivity activity;

    SensorHandlerService(MainActivity activity){
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "onLocationChanged triggered");
        activity.loc = location; // TODO - this is being used from 2 classes, possible concurrency issues?
    }

    float[] mGravity;
    float[] mGeomagnetic;
    private double azimuth;
    private double baseAzimuth;

    public void onSensorChanged(SensorEvent event) {
        if (activity.loc == null)
            return;
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) mGravity = event.values;
        if (Sensor.TYPE_MAGNETIC_FIELD == event.sensor.getType()) mGeomagnetic = event.values;
        if (mGravity == null || mGeomagnetic == null) return;
        float R[] = new float[9];
        float I[] = new float[9];
        boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                mGeomagnetic);
        if (success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);
            baseAzimuth = Math.toDegrees(orientation[0]); // orientation
            // contains:
            // azimut,
            // pitch and roll
            baseAzimuth = (baseAzimuth + 360) % 360;
        }
        azimuth = baseAzimuth;
        activity.editLocation.setText("");
        activity.pb.setVisibility(View.INVISIBLE);
        String longitude = "Longitude: " + activity.loc.getLongitude();
        // Log.v(TAG, longitude);
        String latitude = "Latitude: " + activity.loc.getLatitude();
        // Log.v(TAG, latitude);

        float distance = activity.loc.distanceTo(activity.goal);
        float bearing = activity.loc.bearingTo(activity.goal);
        Log.v(TAG, "getting geofield");
        GeomagneticField geoField = new GeomagneticField(Double.valueOf(
                activity.loc.getLatitude()).floatValue(), Double.valueOf(
                activity.loc.getLongitude()).floatValue(), Double.valueOf(
                activity.loc.getAltitude()).floatValue(), System.currentTimeMillis());

        Log.v(TAG, "Got geofield");
        azimuth -= geoField.getDeclination(); // converts magnetic north
        // into true north

        if (bearing < 0) {
            bearing = bearing + 360;
        }

        // This is where we choose to point it
        Double direction = bearing - azimuth;

        // If the direction is smaller than 0, add 360 to get the rotation
        // clockwise.
        if (direction < 0) {
            direction = direction + 360;
        }

        String bearingText = "N";

        if ((360 >= baseAzimuth && baseAzimuth >= 337.5)
                || (0 <= baseAzimuth && baseAzimuth <= 22.5))
            bearingText = "N";
        else if (baseAzimuth > 22.5 && baseAzimuth < 67.5)
            bearingText = "NE";
        else if (baseAzimuth >= 67.5 && baseAzimuth <= 112.5)
            bearingText = "E";
        else if (baseAzimuth > 112.5 && baseAzimuth < 157.5)
            bearingText = "SE";
        else if (baseAzimuth >= 157.5 && baseAzimuth <= 202.5)
            bearingText = "S";
        else if (baseAzimuth > 202.5 && baseAzimuth < 247.5)
            bearingText = "SW";
        else if (baseAzimuth >= 247.5 && baseAzimuth <= 292.5)
            bearingText = "W";
        else if (baseAzimuth > 292.5 && baseAzimuth < 337.5)
            bearingText = "NW";
        else
            bearingText = "?";

        String s = longitude + "\n" + latitude + "\n\nDistance to goal: "
                + distance + " m" + "\n\nDirection to goal: " + direction
                + "\nBearing: " + bearing + "\nAzimuth: " + azimuth
                + "\nBase azimuth: " + baseAzimuth
                + "\nCompass direction: " + bearingText + "\n"
                + Integer.toString(activity.counter);
        activity.counter++;
        activity.editLocation.setText(s);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}

