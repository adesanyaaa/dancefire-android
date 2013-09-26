package edu.stanford.lazaro.dancefire;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DanceFireService extends Service implements SensorEventListener  {
	private static final int NUM_SAMPLES = 3;
	
	private final AQuery aq = new AQuery(this);
	
	// Binder given to clients
    private final IBinder mBinder = new DanceFireServiceBinder();
    
    private SensorManager mSensorManager;
    private Sensor mSensor;
    
	private double[] forceSamples;
	private int currentSample = 0;
    private double force;
    //private double baselineForce;
    
    private boolean sendHttp = true;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class DanceFireServiceBinder extends Binder {
    	DanceFireService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DanceFireService.this;
        }
    }

    @Override
    public void onCreate() {
    	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	forceSamples = new double[NUM_SAMPLES];
    	force = 0.0;    	
    }

    @Override
    public IBinder onBind(Intent intent) {
    	mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return mBinder;
    }
    
    public double eventToPower(SensorEvent event) {
    	double power = -9.8;
    	
    	power += Math.abs(event.values[0]);
    	power += Math.abs(event.values[1]);
    	power += Math.abs(event.values[2]);
    	
    	if(power < 0) power = 0.0;
    	
    	return power;
    }
    
    private void updateForce(double power) {
    	forceSamples[currentSample] = power;
    	currentSample = (currentSample + 1) % NUM_SAMPLES;
    	force = 0;
    	for(double v : forceSamples) {
    		force += v;
    	}
    	force /= NUM_SAMPLES;
    	
    	if(sendHttp) {
    		sendHttp = false;
	    	String url = "http://dancefire-c9-lazaro_clapp.c9.io/android";
	        Map<String, Object> params = new HashMap<String, Object>();
		    params.put("force", force);
		    aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
		    	@Override
		        public void callback(String url, JSONObject json, AjaxStatus status) {
		            Log.i("DanceFire","Server returned: " + json);
		            sendHttp = true;
		        }
		    });
    	}
    }
    
    @Override
    public void onSensorChanged(SensorEvent event){
    	double power = eventToPower(event);
    	updateForce(power);
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Don't care. For now.
	}

    /** method for clients */
    public double getCurrentAccelerationForce() {
    	return force;
    }

}
