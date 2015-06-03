package org.mconf.android.core.video;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.client.ClientHandshakeHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.Control;

public class MainRtmpConnection extends RtmpConnection {

	private static final Logger log = LoggerFactory.getLogger(MainRtmpConnection.class);
    private boolean connected = false;
	public MainRtmpConnection(ClientOptions options, BigBlueButtonClient context) {
		super(options, context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ChannelPipelineFactory pipelineFactory() {
		// TODO Auto-generated method stub
		return new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
            final ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("handshaker", new ClientHandshakeHandler(options));
            pipeline.addLast("decoder", new RtmpDecoder());
            pipeline.addLast("encoder", new RtmpEncoder());
            pipeline.addLast("handler", MainRtmpConnection.this);
            return pipeline;
            }
		};
	}
	
	@Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
            
            // * https://github.com/bigbluebutton/bigbluebutton/blob/master/bigbluebutton-client/src/org/bigbluebutton/main/model/users/NetConnectionDelegate.as#L102
            // * _netConnection.connect(?);
                             
            Log.e("channel connection","success");
            writeCommandExpectingResult(e.getChannel(), Command.connect(options));
    }
	
	@Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            super.channelDisconnected(ctx, e);
            log.debug("Rtmp Channel Disconnected");
            
            connected = false;
    }
	
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent me) {
    final Channel channel = me.getChannel();
    final RtmpMessage message = (RtmpMessage) me.getMessage();
    Log.e("","<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<,");
    switch(message.getHeader().getMessageType()) {
            case CONTROL:
            Control control = (Control) message;
            switch(control.getType()) {
                case PING_REQUEST:
                    final int time = control.getTime();
                    Control pong = Control.pingResponse(time);
                    channel.write(pong);
                    break;
            }
                    break;
            
            case COMMAND_AMF0:
            case COMMAND_AMF3:
                Command command = (Command) message;                
                String name = command.getName();
                log.debug("server command: {}", name);
                break;
                
            case SHARED_OBJECT_AMF0:
            case SHARED_OBJECT_AMF3:
                    //onSharedObject(channel, (SharedObjectMessage) message);
            		Log.d("object", "shared");
                    break;
            default:
                	log.info("ignoring rtmp message: {}", message);
                    break;
    	}
    }
	
	public boolean isConnected() {
        return connected;
	}
}