package org.mconf.android.core.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;

public class CaptureDialog extends Dialog {	
	private static final Logger log = LoggerFactory.getLogger(CaptureDialog.class);
		
	private VideoCapture videoWindow;
	private boolean startsHidden = true; //true=capture starts without preview. false=capture starts with preview.
	public boolean isPreviewHidden;
	private boolean wasPreviewHidden = false;
	
	public CaptureDialog(Context context) {
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes the title from the Dialog
		setContentView(R.layout.video_capture);
		
		videoWindow = (VideoCapture) findViewById(R.id.video_capture);		
		
		if(startsHidden){
			isPreviewHidden = false;
			hidePreview();
		} else {
			isPreviewHidden = true;
			showPreview(true);
		}
			
		setTitle("Camera preview");
		setCancelable(false);
	}

	public void hidePreview() { //hides the preview but keeps capturing
		if(!isPreviewHidden){   	
			android.view.WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();	  
		    windowAttributes.flags = android.view.WindowManager.LayoutParams.FLAG_SCALED
		    					| android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		    					| android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
		    ;	  
		    getWindow().setAttributes(windowAttributes);
		  
		    if(videoWindow != null){
			    videoWindow.hidePreview();
		    }
		  
		    isPreviewHidden = true;
		}
	}
	
	public void showPreview(boolean center) { //if the preview is hidden, show it
		if(isPreviewHidden){			
			android.view.WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();		
			windowAttributes.flags = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; //Makes the video brigth
			getWindow().setAttributes(windowAttributes);
			
			if(videoWindow != null && center){
				centerPreview();
			}
			
			isPreviewHidden = false;
		}
	}
	
	public void centerPreview(){
		if(videoWindow != null){
			videoWindow.centerPreview();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		resume();
	}
	
	@Override
	protected void onStop() {
		pause();
		super.onStop();
	}

	public void pause() {
		if(isPreviewHidden){
			showPreview(false); //this is needed to avoid a crash when closing the dialog
			wasPreviewHidden = true;
		} else {
			wasPreviewHidden = false;
		}
	}
	
	public void resume() {
		if(wasPreviewHidden){ //if the preview was hidden before the onStop,
							  //then lets hide it again after the Dialog is resumed
			hidePreview();
		}
	}
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event){
		super.onKeyDown(keyCode, event);
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			hidePreview();
        }
		
		return true;
	}
}