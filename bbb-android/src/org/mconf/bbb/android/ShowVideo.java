/*
 * GT-Mconf: Multiconference system for interoperable web and mobile
 * http://www.inf.ufrgs.br/prav/gtmconf
 * PRAV Labs - UFRGS
 * 
 * This file is part of Mconf-Mobile.
 *
 * Mconf-Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mconf-Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mconf-Mobile.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mconf.bbb.android;

import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.IVideoListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;



public class ShowVideo extends Activity implements IVideoListener {
	private static final Logger log = LoggerFactory.getLogger(ShowVideo.class);
	private GLSurfaceView_mconf mGLView = null;
	private DisplayMetrics metrics = null;
	private int width=0,height=0;
	private static boolean DEBUG = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.debug("onCreate");
		
		Client.bbb.addListener(this);
		
		//gets the screen resolution and adjusts the screen accordingly
		metrics = new DisplayMetrics();
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);        
        width = metrics.widthPixels;
        height = metrics.heightPixels;        
        changeOrientation(width,height);        
        if(DEBUG){
        	log.debug("ShowVideo class", String.format("Screen resolution: %d X %d\n",width, height));
        }
		
        //prepares the surface to render the video
		if(mGLView==null){
			mGLView = new GLSurfaceView_mconf(this,width,height);
		} else {
			Log.v("ShowVideo Class", String.format("Error: mGLView should be null but isnt!\n"));
		} 
		setContentView(mGLView);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	
		stopThreads();//stops the C++ code, otherwise it will keep running
	}	
	
	@Override
	protected void onDestroy() {
		//TODO Gian implement the onDestroy for ShowVideo.java
		log.debug("onDestroy");
//		
		Client.bbb.removeListener(this);
//		bbb.disconnect();
//
//		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		notificationManager.cancelAll();	
//
////		unregisterReceiver(receiver);
//
		super.onDestroy();
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//TODO Gian implement the onKeyDown for ShowVideo.java
//    	if (keyCode == KeyEvent.KEYCODE_BACK) {
//    		Intent intent = new Intent(SEND_TO_BACK);
//    		sendBroadcast(intent);
//    		log.debug("KEYCODE_BACK");
//    		moveTaskToBack(true);
//    		return true;
//    	}    		
    	return super.onKeyDown(keyCode, event);
    }

	
	@Override
	public void onVideo(byte[] data) {
		//insert encoded data into the queue		
		enqueueEncoded(data,data.length);
//		log.debug("new frame");
	}
	
	 static {
    	System.loadLibrary("avutil");
    	System.loadLibrary("swscale");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("thread");
    	System.loadLibrary("common");
    	System.loadLibrary("queue");
    	System.loadLibrary("decode");
    	System.loadLibrary("mconfnative");  
        
    	if(DEBUG){
    		Log.v("Java ShowVideo class", String.format("native libraries loaded\n"));
    	}
    }

	native void changeOrientation(int w,int h);
	native void stopThreads();
	native void enqueueEncoded(byte[] Data, int length);
}