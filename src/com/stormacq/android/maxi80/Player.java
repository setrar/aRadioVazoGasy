//
//  Player.java
//  Maxi80
//
//  Created by Sebastien Stormacq on 13/06/2010.
//
//
// This file is part of Maxi80 Android Application.
// 
// Maxi80 Android App is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 2 of the License 
// 
// Maxi80 Android App is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the Licenses
// along with Maxi80 Android App.  If not, see <http://www.gnu.org/licenses/>
//
//

package com.stormacq.android.maxi80;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

class InfoUpdate implements MediaPlayer.OnInfoListener {

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.d(getClass().getName(), "onInfo : " + what + "," + extra);
		return false;
	}
	
}

public class Player {

	private MediaPlayer mp = null;
	private boolean isPlaying = false;
	private PlayerListener playerListener = null;
	private String audioStreamURL = null;
	
	public Player(String audioStreamURL) {
		this.audioStreamURL = audioStreamURL;
	}
	
	public synchronized boolean isPlaying() {
		return isPlaying;
	}
	
	public synchronized void start(Context ctx) {
		
		if (mp == null) {
			mp = prepareMediaPlayer(ctx);
		} 
		
		isPlaying = true;
		mp.start();
		Log.i(getClass().getName(), "Playing");
	}
	
	public synchronized void stop() {
		if (mp != null) {
			isPlaying = false;
			mp.stop();
			mp.release();
			mp = null;
			Log.i(getClass().getName(), "Stopped");
		}
	}
	
	public void setPlayerListener(PlayerListener pl) {
		if (pl == null)
			throw new IllegalArgumentException("Can not pass a null PlayerListener");
		playerListener = pl;
	}

	private MediaPlayer prepareMediaPlayer(Context ctx) {
		
		MediaPlayer result;
		Log.d(getClass().getName(), "Creating a MediaPlayer");
		result = new MediaPlayer();
		
		try {
			Log.d(getClass().getName(), "Adding an Info Listener");
			result.setOnInfoListener(new InfoUpdate());

			Log.d(getClass().getName(), "Adding an ErrorListener");
			result.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mediaplayer, int what, int extra) {
					
					//stopping the player
					if (mp != null) {
						mp.stop();
						mp.release();
						mp = null;
						isPlaying = false;
					}
					
					playerListener.onError(what);
					Log.i(getClass().getName(), "Error : " + what);
					return true;
				}
			});
			
			Log.d(getClass().getName(), "Adding a BufferingListener");
			result.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
				
				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					//Log.i(getClass().getName(), "Buffering : " + percent + "%");
					
				}
			});
			
			Log.d(getClass().getName(), "Adding a  PrepareListener");
			result.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					Log.i(getClass().getName(), "Prepared");
					
				}
			});
						
			Log.d(getClass().getName(), "Setting the data source");
			result.setDataSource(audioStreamURL);
			/*
			Map<String,String> headers = new HashMap<String, String>();
			headers.put("icy-metadata", "1");
			result.setDataSource(ctx, Uri.parse(AUDIO_STREAM_URL), headers);
			*/
			result.setAudioStreamType(AudioManager.STREAM_MUSIC);

			Log.d(getClass().getName(), "Preparing the player");
			result.prepare();
			
		} catch (Exception e) {
			Log.e(getClass().getName(), "Can not initialize data source : " + audioStreamURL, e);
		}
		
		return result;
	}
	
}
