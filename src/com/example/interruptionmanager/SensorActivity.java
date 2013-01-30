package com.example.interruptionmanager;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {

	private TextView txtMicrophoneLevelValue;
	private TextView txtLightLevelValue;
	private TextView txtAccXValue;
	private TextView txtAccYValue;
	private TextView txtAccZValue;
	
	private MediaRecorder mRecorder;
	private Handler sHandler;
	
	private SensorManager mSensorManager;
	private Sensor mLight;
	private Sensor mAccelerometer;
	private Sensor mGravity;
	
	private double xGravity, yGravity, zGravity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensors);
		txtMicrophoneLevelValue = (TextView) findViewById(R.id.txtMicrophone);
		txtLightLevelValue = (TextView) findViewById(R.id.txtLight);
		txtAccXValue = (TextView) findViewById(R.id.txtAccelerometerX);
		txtAccYValue = (TextView) findViewById(R.id.txtAccelerometerY);
		txtAccZValue = (TextView) findViewById(R.id.txtAccelerometerZ);
		
		setupMic();

		sHandler = new Handler();
		sHandler.removeCallbacks(updateVolumeValue);
		sHandler.postDelayed(updateVolumeValue, 100);
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	}

	@Override
	protected void onResume() {
		mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	private void setupMic() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");
			try {
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mRecorder.start();
		}
	}

	private Runnable updateVolumeValue = new Runnable() {
		public void run() {
			double micValue = getAmplitude();
			txtMicrophoneLevelValue.setText("Microphone level: "+Double.toString(micValue));

			sHandler.postDelayed(this, 100);
		};
	};

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude() / 2700.0);
		else
			return 0;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if( event.sensor.getType() == Sensor.TYPE_LIGHT){
			txtLightLevelValue.setText("Light value: "+Double.toString(event.values[0]));
		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			txtAccXValue.setText("Accelerometer X-value: "+Double.toString(event.values[0]-xGravity));
			txtAccYValue.setText("Accelerometer Y-value: "+Double.toString(event.values[1]-yGravity));
			txtAccZValue.setText("Accelerometer Z-value: "+Double.toString(event.values[2]-zGravity));
		} else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			xGravity = event.values[0];
			yGravity = event.values[1];
			zGravity = event.values[2];
		}
	}
}
