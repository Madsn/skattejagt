package com.noptech.android.skattejagt;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private int counter = 0;
	private LocationManager locationManager = null;
	private SensorManager sensorManager = null;
	private MySensorListener sensorListener = null;
	private Location loc;

	private Button btnGetLocation = null;
	private EditText editLocation = null;
	private ProgressBar pb = null;

	private static final String TAG = "Debug";
	private Boolean flag = false;

	private Location goal = new Location("dummyprovider");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// if you want to lock screen for always Portrait mode
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		goal.setLatitude(56.16);
		goal.setLongitude(10.2);

		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.INVISIBLE);

		editLocation = (EditText) findViewById(R.id.editTextLocation);

		btnGetLocation = (Button) findViewById(R.id.btnLocation);
		btnGetLocation.setOnClickListener(this);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

	}

	@Override
	public void onClick(View v) {
		flag = displayGpsStatus();
		if (flag) {

			Log.v(TAG, "onClick");

			editLocation
					.setText("Please!! move your device to see the changes in coordinates."
							+ "\nWait..");

			pb.setVisibility(View.VISIBLE);
			
			sensorListener = new MySensorListener();
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 10, sensorListener);
			sensorManager.registerListener((SensorEventListener) sensorListener, 
					sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
					SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			alertbox("Gps Status!!", "Your GPS is: OFF");
		}

	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	private Boolean displayGpsStatus() {
		ContentResolver contentResolver = getBaseContext().getContentResolver();
		boolean gpsStatus = false;
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			try {
				gpsStatus = Settings.Secure.getInt(contentResolver,
						Settings.Secure.LOCATION_MODE) == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			gpsStatus = Settings.Secure.isLocationProviderEnabled(
					contentResolver, LocationManager.GPS_PROVIDER);
		}
		if (gpsStatus) {
			return true;
		} else {
			return false;
		}
	}

	/*----------Method to create an AlertBox ------------- */
	protected void alertbox(String title, String mymessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your Device's GPS is Disable")
				.setCancelable(false)
				.setTitle("** Gps Status **")
				.setPositiveButton("Gps On",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// finish the current activity
								// AlertBoxAdvance.this.finish();
								Intent myIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(myIntent);
								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// cancel the dialog box
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class MySensorListener implements SensorEventListener, LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.v(TAG, "onLocationChanged triggered");
			loc = location;
		}
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			//Log.v(TAG, "onSensorChanged triggered");
			if (loc == null) {
				Log.v(TAG, "Location is null");
				return;
			}
			if (event.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD) {
				Log.v(TAG, "Sensor type is not magnetic field, but: "
						+ event.sensor.getType());
				return;
			}
			//Log.v(TAG, "Event values: " + event.values[0]);
			
			float azimuth = event.values[0];
			float baseAzimuth = azimuth;

			editLocation.setText("");
			pb.setVisibility(View.INVISIBLE);
			/*
			Toast.makeText(
			 					getBaseContext(),
					"Location changed : Lat: " + loc.getLatitude() + " Lng: "
							+ loc.getLongitude(), Toast.LENGTH_SHORT).show();
			*/
			String longitude = "Longitude: " + loc.getLongitude();
			//Log.v(TAG, longitude);
			String latitude = "Latitude: " + loc.getLatitude();
			//Log.v(TAG, latitude);
			
			float distance = loc.distanceTo(goal);
			float bearing = loc.bearingTo(goal);
			Log.v(TAG, "getting geofield");
			GeomagneticField geoField = new GeomagneticField(Double.valueOf(
					loc.getLatitude()).floatValue(), Double.valueOf(
					loc.getLongitude()).floatValue(), Double.valueOf(
					loc.getAltitude()).floatValue(), System.currentTimeMillis());

			Log.v(TAG, "Got geofield");
			azimuth -= geoField.getDeclination(); // converts magnetic north
													// into true north

			if (bearing < 0) {
				bearing = bearing + 360;
			}

			// This is where we choose to point it
			float direction = bearing - azimuth;

			// If the direction is smaller than 0, add 360 to get the rotation
			// clockwise.
			if (direction < 0) {
				direction = direction + 360;
			}
			
			String bearingText = "N";

		    if ( (360 >= baseAzimuth && baseAzimuth >= 337.5) || (0 <= baseAzimuth && baseAzimuth <= 22.5) ) bearingText = "N";
		    else if (baseAzimuth > 22.5 && baseAzimuth < 67.5) bearingText = "NE";
		    else if (baseAzimuth >= 67.5 && baseAzimuth <= 112.5) bearingText = "E";
		    else if (baseAzimuth > 112.5 && baseAzimuth < 157.5) bearingText = "SE";
		    else if (baseAzimuth >= 157.5 && baseAzimuth <= 202.5) bearingText = "S";
		    else if (baseAzimuth > 202.5 && baseAzimuth < 247.5) bearingText = "SW";
		    else if (baseAzimuth >= 247.5 && baseAzimuth <= 292.5) bearingText = "W";
		    else if (baseAzimuth > 292.5 && baseAzimuth < 337.5) bearingText = "NW";
		    else bearingText = "?";

			String s = longitude + "\n" + latitude + "\n\nDistance to goal: "
					+ distance + " m" + "\n\nDirection to goal: " + direction
					+ "\nBearing: " + bearing + "\nAzimuth: " + azimuth
					+ "\nBase azimuth: " + baseAzimuth
					+ "\nCompass direction: " + bearingText + "\n"+ Integer.toString(counter);
			counter++;
			editLocation.setText(s);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

	}

}
