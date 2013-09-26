package edu.stanford.lazaro.dancefire;

import java.util.Timer;
import java.util.TimerTask;

import edu.stanford.lazaro.dancefire.DanceFireService.DanceFireServiceBinder;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private DanceFireService mService;
	private boolean mBound = false;
	
	private Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {          
            @Override
            public void run() {
            	MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	updateDisplay();
                    }
            	});
            }

        }, 0, 1000);
    }

    protected void updateDisplay() {
    	Log.i("DanceFire", "Timer tick");
    	if(mBound) {
        	Log.i("DanceFire", "Updating display");
			double force = mService.getCurrentAccelerationForce();
	    	TextView text = (TextView)findViewById(R.id.acceleration_display);
	    	text.setText(String.format("%.7f", force));
    	}
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(mBound) {
            		button.setText(R.string.start_button_label);
            		unbindService(mConnection);
            		mBound = false;
            	} else {
            		button.setText(R.string.start_button_label_on);
            		Intent intent = new Intent(MainActivity.this, DanceFireService.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            	}
            }
        });
    }
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	DanceFireServiceBinder binder = (DanceFireServiceBinder) service;
            mService = binder.getService();
        	Log.i("DanceFire", "Service bound");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	Log.i("DanceFire", "Service disconnected");
            mBound = false;
        }
    };

}
