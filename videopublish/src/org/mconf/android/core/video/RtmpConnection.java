package org.mconf.android.core.video;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import com.flazr.rtmp.client.ClientHandler;
import com.flazr.rtmp.client.ClientOptions;
import com.flazr.rtmp.message.ChunkSize;
import com.flazr.rtmp.message.Command;
import com.flazr.rtmp.message.CommandAmf0;
import com.flazr.rtmp.message.Control;

public abstract class RtmpConnection extends ClientHandler implements ChannelFutureListener {

	private static final Logger log = LoggerFactory.getLogger(RtmpConnection.class);
	
	final protected BigBlueButtonClient context;
	
	public RtmpConnection(ClientOptions options, BigBlueButtonClient context) {
		super(options);
		Log.e("",options.toString());
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	private ClientBootstrap bootstrap = null;
    private ChannelFuture future = null;
    private ChannelFactory factory = null;
	
	@Override
	public void operationComplete(ChannelFuture arg0) throws Exception {
		// TODO Auto-generated method stub
		
		if (future.isSuccess()){
			Log.e("", "jjjjjjjjjjjjjjjjjjjj");

			onConnectedSuccessfully();
		}
		else{
			Log.e("", "wwwweeeeeeeeeeeeeee");
			onConnectedUnsuccessfully();
		}
	}

	private void onConnectedUnsuccessfully() {
		// TODO Auto-generated method stub
		
	}

	private void onConnectedSuccessfully() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean connect() {
        	if(factory == null)
        		factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        	bootstrap = new ClientBootstrap(factory);
        	bootstrap.setPipelineFactory(pipelineFactory());
        	future = bootstrap.connect(new InetSocketAddress(options.getHost(),options.getPort()));
        	future.addListener(this);
        	
        	//Log.e("a>>>>>>>>>>>>>>>",future.getChannel().getRemoteAddress().toString());
        	return true;
        }

	abstract protected ChannelPipelineFactory pipelineFactory();

	public void disconnect() {
        if (future != null) {
                if (future.getChannel().isConnected()) {
                		log.debug("Channel is connected, disconnecting");
                        //future.getChannel().close(); //ClosedChannelException
                        future.getChannel().disconnect();
                        future.getChannel().getCloseFuture().awaitUninterruptibly();
                }
                future.removeListener(this);
                factory.releaseExternalResources();
                future = null; factory = null; bootstrap = null;
        	}
    	}
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            String exceptionMessage = e.getCause().getMessage();
            if (exceptionMessage != null && exceptionMessage.contains("ArrayIndexOutOfBoundsException") && exceptionMessage.contains("bad value / byte: 101 (hex: 65)")) {
            		Log.e("","wwwwwwwwwwwwwww");
            		log.debug("Ignoring malformed metadata");
                    return;
            } else {
                	Log.e("",""+exceptionMessage);
            		super.exceptionCaught(ctx, e);
            }
    }
	public void doGetMyUserId(Channel channel) {
    	Command command = new CommandAmf0("updateStreamn", null, "Test1","AV");
    	Log.e("","updateStreamn");
    	writeCommandExpectingResult(channel, command);
    }
	
	@Override
	protected void onCommandStatus(Channel channel, Command command,
			Map<String, Object> args) {
        final String code = (String) args.get("code");
        final String level = (String) args.get("level");
        final String description = (String) args.get("description");
        final String application = (String) args.get("application");
        final String messageStr = level + " onStatus message, code: " + code + ", description: " + description + ", application: " + application;
        
        // http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/flash/events/NetStatusEvent.html
        if (level.equals("status")) {
        	logger.info(messageStr);
            if (code.equals("NetStream.Publish.Start")
            		&& publisher != null && !publisher.isStarted()) {
        		logger.debug("starting the publisher after NetStream.Publish.Start");
            	publisher.start(channel, options.getStart(), options.getLength(), new ChunkSize(4096));
            	doGetMyUserId(channel);
            } else if (code.equals("NetStream.Unpublish.Success")
            		&& publisher != null) {
                logger.info("unpublish success, closing channel");
                ChannelFuture future = channel.write(Command.closeStream(streamId));
                future.addListener(ChannelFutureListener.CLOSE);
            } else if (code.equals("NetStream.Play.Stop")) {
            	channel.close();
            }
        } else if (level.equals("warning")) {
        	logger.warn(messageStr);
        	if (code.equals("NetStream.Play.InsufficientBW")) {
                ChannelFuture future = channel.write(Command.closeStream(streamId));
                future.addListener(ChannelFutureListener.CLOSE);
                // \TODO create a listener for insufficient bandwidth
        	}
        } else if (level.equals("error")) {
        	logger.error(messageStr);
            channel.close();
        }
	}
	
	@Override
	protected void onControl(Channel channel, Control control) {
		if (control.getType() != Control.Type.PING_REQUEST)
			logger.debug("control: {}", control);
        switch(control.getType()) {
            case PING_REQUEST:
                final int time = control.getTime();
                Control pong = Control.pingResponse(time);
                // we don't want to print two boring messages every second
//                logger.debug("server ping: {}", time);
//                logger.debug("sending ping response: {}", pong);
                if (channel.isWritable())
                	channel.write(pong);
                break;
            case SWFV_REQUEST:
                if(swfvBytes == null) {
                    logger.warn("swf verification not initialized!" 
                        + " not sending response, server likely to stop responding / disconnect");
                } else {
                    Control swfv = Control.swfvResponse(swfvBytes);
                    logger.info("sending swf verification response: {}", swfv);
                    channel.write(swfv);
                }
                break;
            case STREAM_BEGIN:
                if(publisher != null && !publisher.isStarted()) {
                    publisher.start(channel, options.getStart(),
                            options.getLength(), new ChunkSize(4096));
                    return;
                }
                //if(streamId !=0) {
                    //channel.write(Control.setBuffer(streamId, options.getBuffer()));
                    channel.write(Control.setBuffer(1, options.getBuffer()));

                //}
                break;
            default:
                logger.debug("ignoring control message: {}", control);
        }
	}
}