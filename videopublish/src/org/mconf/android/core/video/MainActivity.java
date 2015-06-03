package org.mconf.android.core.video;


import org.apache.log4j.BasicConfigurator;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_capture);
		BasicConfigurator.configure();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		/*BigBlueButtonClient bc=new BigBlueButtonClient();
		bc.connectBigBlueButton();*/
		VideoDialog mVideoDialog = new VideoDialog(this, "1", "1", "Test1", 1);
		mVideoDialog.show();
		/*
		VideoCapture mVideoCapture = (VideoCapture) findViewById(R.id.video_capture);
		mVideoCapture.startCapture();*/
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
