package org.mconf.android.core.video;

import java.util.Collection;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.flazr.util.Utils;

public class BigBlueButtonClient {

	private static final Logger log = LoggerFactory.getLogger(BigBlueButtonClient.class);

	private MainRtmpConnection mainConnection = null;
    //private Object[] args={"Test1","18.9750/72.8258","AB","1","Female","info",""};
    private Object[] args={"Test1"};


	public MainRtmpConnection getConnection() {
		return mainConnection;
	}
	
	public boolean connectBigBlueButton() {
		ClientOptions opt = new ClientOptions();
		opt.setHost("10.129.200.81");
		//opt.setAppName("HariPanTest3");
		opt.setAppName("PanTest");
		opt.setArgs(args);
		mainConnection = new MainRtmpConnection(opt, this);
		return mainConnection.connect();
	}

	public void disconnect() {
		if (mainConnection != null)
			mainConnection.disconnect();
	}

	public static void main(String[] args) {
		BigBlueButtonClient client = new BigBlueButtonClient();
			client.connectBigBlueButton();
	}

	public boolean onCommand(String resultFor, Command command) {
			return true;
	}

	public boolean isConnected() {
		if (mainConnection == null)
			return false;
		else
			return mainConnection.isConnected();
	}
}