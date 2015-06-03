package org.mconf.android.core.video;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Window;

public class VideoDialog extends Dialog {	
	private static final Logger log = LoggerFactory.getLogger(VideoDialog.class);
		
	//private VideoSurface videoWindow;
	private String userId;
	private String name;
	public boolean isPreview;
	private int streamToShow;
	
	public VideoDialog(Context context, String userId, String myId, String name, int streamToShow) {
		super(context);
		
		this.userId = userId;
		this.name = name;
		
		if(userId.equals(myId)){
			isPreview = true;
		} else {
			isPreview = false;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes the title from the Dialog
		
		if(isPreview){
			setContentView(R.layout.video_capture);
		} else {
			/*setContentView(R.layout.video_window);
			
			videoWindow = (VideoSurface) findViewById(R.id.video_window);*/
		}
		
		android.view.WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();		
		windowAttributes.flags = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; //Makes the video brigth
//		windowAttributes.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; //Makes it possible to interact with the window behind, but the video should be closed properly when the screen changes
//		windowAttributes.flags = android.view.WindowManager.LayoutParams.FLAG_SCALED; //Removes the title from the dialog and removes the border also		 
		getWindow().setAttributes(windowAttributes);
		
		setTitle(name);
		setCancelable(true);
		
		this.streamToShow = streamToShow;
	}
	
	private void sendBroadcastRecreateCaptureSurface() {
		log.debug("sendBroadcastRecreateCaptureSurface()");
		
		//Intent intent= new Intent(Client.CLOSE_DIALOG_PREVIEW);
		//getContext().sendBroadcast(intent);
	}
	
	private void setVideoId(String userIdLocal){
		userId = userIdLocal;
	}
	
	private void setVideoName(String userName){
		name = userName;
	}
	
	public String getVideoId(){
		return userId;
	}
	
	public String getVideoName(){
		return name;
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
		if(isPreview){
			sendBroadcastRecreateCaptureSurface();
		} else {
			//videoWindow.stop();
		}
	}
	
	public void resume() {
		if(isPreview){
			VideoCaptureLayout videocaplayout = (VideoCaptureLayout) findViewById(R.id.video_capture_layout);
			videocaplayout.show(40);
		} else {
			//videoWindow.start(userId, true, streamToShow);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();

		VideoCapture mVideoCapture = (VideoCapture) findViewById(R.id.video_capture);
		mVideoCapture.startCapture();
	}
}