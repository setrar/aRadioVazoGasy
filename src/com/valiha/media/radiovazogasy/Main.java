//
//  Main.java
//  RadioToko3
//
//  Created by Setra R on 03/28/2013.
//
//
// This file is part of RadioToko3 Android Application.
// 
// RadioToko3 Android App is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 2 of the License 
// 
// RadioToko3 Android App is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the Licenses
// along with RadioToko3 Android App.  If not, see <http://www.gnu.org/licenses/>
//
//

package com.valiha.media.radiovazogasy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.stormacq.android.maxi80.Player;
import com.stormacq.android.maxi80.PlayerListener;


public class Main extends Activity implements SensorEventListener {
	
	private ImageButton   streamButton;
	private SeekBar  	  seekbarVolume;
	private WebView  	  webview;
	
	private Player player;
	private boolean onError = false;
	
	private SensorManager sensorManager;
	private Sensor proximitySensor;

	/***************************************************************************************************
	 * PLAYBACK CONTROL HANDLING
	 ***************************************************************************************************/

	private synchronized void startPlaying() {
		
		Log.d(getClass().getName(), "startPlaying");

		//execute the play method on a background thread for responsiveness
		Log.d(getClass().getName(), "animating webView IN");
		Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		webview.startAnimation(fadeInAnimation );
		webview.setVisibility(View.VISIBLE);
		webview.reload();

		streamButton.setImageResource(android.R.drawable.ic_media_pause);
		streamButton.requestFocus();

		onError = false;
		final Context ctx = this;
				
		new Thread(new Runnable() {
		    public void run() {
		    	player.start(ctx);	
		    }
		  }).start();
		
	}
	
	private synchronized void stopPlaying() {
		
		Log.d(getClass().getName(), "stopPlaying");

		Log.d(getClass().getName(), "animating webView OUT");
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		webview.startAnimation(fadeOutAnimation);
		webview.setVisibility(View.INVISIBLE);

		player.stop();

		streamButton.setImageResource(android.R.drawable.ic_media_play);
				
	}

	/***************************************************************************************************
	 * LIFE CYCLE HANDLING
	 ***************************************************************************************************/

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		Log.d(getClass().getName(), "onCreate");

		setContentView(R.layout.main);
		initControls();
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);		
	}
	

	public void onStart() {
		super.onStart();
		Log.i(getClass().getName(), "onStart");
		
		if (player == null) {
			player = new Player(RadioConfig.AUDIO_STREAM_URL);
			final Activity thisActivity = this; //just to give to the inner class
			player.setPlayerListener(new PlayerListener() {
				
				@Override
				public void onError(int what) {
					
					Log.w(getClass().getName(), "player - onError");
					
					if (!onError) {
						//on error, the player is paused, set the button back to "Play"
						streamButton.setImageResource(android.R.drawable.ic_media_play);
						
						//notifying user
						new AlertDialog.Builder(thisActivity)
							   .setMessage(R.string.error_cannot_connect)
						       .setCancelable(false)
						       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						        	   dialog.cancel();
						           }
						       }).show();
						
						//hide cover
						webview.setVisibility(View.INVISIBLE);
						
						onError = true;
					}
				}
			});
		}
		
		if (player != null) {
			startPlaying();
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		Log.d(getClass().getName(), "onDestroy");
		

		if (player != null) {
			stopPlaying();
			player = null;
		}
		
	}
	
	private void initControls() {
		
		Log.d(getClass().getName(), "initControls");

		webview = (WebView) findViewById(R.id.webview);
		webview.setBackgroundColor(0);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl(RadioConfig.WEB_STREAM_URL);
		webview.setFocusable(false);
		
		streamButton = (ImageButton) findViewById(R.id.button_stream);
		streamButton.setFocusable(true);
		streamButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {

					if (!player.isPlaying()) {
						startPlaying();
					} else {
						stopPlaying();
					}
					
				} catch (Exception e) {
					Log.e(getClass().getName(), "Can not play/pause stream", e);
					player.stop();
					streamButton.setImageResource(android.R.drawable.ic_media_play);
					Log.i(getClass().getName(), "Forced Stopped");
				}
			}
		});
		
		
		seekbarVolume = (SeekBar) findViewById(R.id.seekbar_volume);
		Context context = getApplicationContext();
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		final int MAX_VOLUME = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		seekbarVolume.setMax(MAX_VOLUME);
		
		//set initial volume at 50% as per @lio347 request
		am.setStreamVolume(AudioManager.STREAM_MUSIC, MAX_VOLUME / 2, 0);
		seekbarVolume.setProgress(MAX_VOLUME / 2);
		seekbarVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				AudioManager am = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
				am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
			}
		});
		
	}
	
	
/***************************************************************************************************
 * MENU HANDLING
 ***************************************************************************************************/
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		  
		switch(item.getItemId()) {
			case R.id.quit:
				this.finish();
				break;
			case R.id.send_email:
				sendEmail();
			  break;
		}
		return super.onContextItemSelected(item); 
	}	

	private void sendEmail() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("plain/text");
	    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{RadioConfig.CONTACT_URL,RadioConfig.CONTACT_URL});
	    intent.putExtra(Intent.EXTRA_SUBJECT, RadioConfig.RADIO_NAME + " on Android");
	    startActivity(intent);	
	}

	/***************************************************************************************************
	 * PROXIMITY SENSOR HANDLING
	 ***************************************************************************************************/
	
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		
		//Log.d(getClass().getName(), "onSensorChanged");

		float distanceInCm = event.values[0];
		if (distanceInCm < 1.0f && player.isPlaying()) {
			stopPlaying();
			return;
		}
		if (distanceInCm >= 1.0f && !player.isPlaying()) {
			startPlaying();
			return;
		}
		
	}

	  @Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, proximitySensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	/***************************************************************************************************
	 * VOLUME BUTTON HANDLING
	 ***************************************************************************************************/
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		Log.d(getClass().getName(), "onKeyDown");
		
		switch (keyCode) {
	    case KeyEvent.KEYCODE_VOLUME_UP:
	    	seekbarVolume.setProgress(seekbarVolume.getProgress() + 1);
	        return true;
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	    	seekbarVolume.setProgress(seekbarVolume.getProgress() - 1);
	        return true;
	    default:
	        return super.onKeyDown(keyCode, event);
	    }
	}	
}
