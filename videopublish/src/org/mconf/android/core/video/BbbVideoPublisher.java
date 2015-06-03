package org.mconf.android.core.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.util.Utils;


public class BbbVideoPublisher {
	private static final Logger log = LoggerFactory.getLogger(BbbVideoPublisher.class);

	private VideoPublisherConnection videoConnection = null;
	private String streamName;
	private BigBlueButtonClient context;
	private ClientOptions opt;
    private Object[] args={"Test1","18.9750/72.8258","AB","1","Female","info",""};
    //private Object[] args={"Test1"};
	
	public BbbVideoPublisher(BigBlueButtonClient context, RtmpReader reader, String streamName) {
		this.streamName = streamName;
		this.context = context;
		
		opt = new ClientOptions();
		opt.setHost("10.129.200.81");
		opt.setAppName("HariPanTest3");
		//opt.setAppName("PanTest");
		opt.publishLive();
		opt.setArgs(args);
		opt.setStreamName(streamName);
		opt.setReaderToPublish(reader);
	}
	
	public void setLoop(boolean loop) {
		opt.setLoop(loop? Integer.MAX_VALUE: 0);
	}
	
	public void start() {
		//context.getUsersModule().addStream(streamName);
		if (videoConnection == null) {
			videoConnection = new VideoPublisherConnection(opt, context);
			videoConnection.connect();
		}
	}
	
	public void stop() {
		//context.getUsersModule().removeStream(streamName);
		// when the stream is removed from the users module, the client automatically
		// receives a NetStream.Unpublish.Success, then the channel is closed
		// \TODO it's may create a memory leak, check it
		//videoConnection.disconnect();
		videoConnection = null;
	}

	public void fireFirstFrame() {
		if (videoConnection != null) {
			videoConnection.publisher.fireNext(videoConnection.publisher.channel, 0);
		}
	}
}