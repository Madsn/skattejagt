package com.noptech.android.skattejagt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
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

	protected int counter = 0;
	private LocationManager locationManager = null;
	private SensorManager sensorManager = null;
	private SensorHandlerService sensorListener = null;
	protected Location loc;

	private Button btnGetLocation = null;
	protected EditText editLocation = null;
	private EditText longitudeTxt = null;
	private EditText latitudeTxt = null;
	protected ProgressBar pb = null;

	private static final String TAG = "Debug";
	private Boolean flag = false;

	protected Location goal = new Location("dummyprovider");
	private Sensor accelerometer;
	private Sensor magnetometer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        if (savedInstanceState == null){
            //TODO - create new state
        } else {
            //TODO - load old state
        }

		// if you want to lock screen for always Portrait mode
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.INVISIBLE);

		editLocation = (EditText) findViewById(R.id.editTextLocation);
		latitudeTxt = (EditText) findViewById(R.id.latitudeTxt);
		longitudeTxt = (EditText) findViewById(R.id.longitudeTxt);

		btnGetLocation = (Button) findViewById(R.id.btnLocation);
		btnGetLocation.setOnClickListener(this);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

	}

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        // TODO - save state
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        editLocation.setText("click 'get location' to start tracking again");
    }

    public void onGetCoordsClick(View v) {
		if (loc == null) {
			Toast.makeText(getBaseContext(), "Location is null",
					Toast.LENGTH_SHORT).show();
			return;
		}
		latitudeTxt.setText(Double.toString(loc.getLatitude()));
		longitudeTxt.setText(Double.toString(loc.getLongitude()));
	}

	public void onSetCoordsClick(View v) {
		if (latitudeTxt.getText().toString() != "" && longitudeTxt.getText().toString() != ""){
			goal.setLatitude(Double.parseDouble(latitudeTxt.getText().toString()));
			goal.setLongitude(Double.parseDouble(longitudeTxt.getText().toString()));
		}
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

			sensorListener = new SensorHandlerService(this);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000, 10, sensorListener);

			accelerometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			magnetometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			sensorManager.registerListener(sensorListener, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
			sensorManager.registerListener(sensorListener, magnetometer,
					SensorManager.SENSOR_DELAY_UI);
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

}
