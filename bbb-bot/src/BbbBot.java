//BigBlueButtonBot, GT-MCONF @PRAV-UFRGS, developed by Arthur C. Rauter, august 2011.
//Adding video to the bots through Xuggler library, September 2011.

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.api.Meeting;
import org.mconf.bbb.video.IVideoPublishListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.RtmpReader;
import com.flazr.rtmp.message.Metadata;
import com.flazr.rtmp.message.Video;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.demos.VideoImage;
import org.mconf.bbb.video.IVideoPublishListener;


public class BbbBot {
		
	//Creates the Master (a BbbBot object), which will spawn the bots.
	public static void main (String[] args) {
		BbbBot Master = new BbbBot(args);
		Master.spawnBots();
		if (Master.getVideoFileName().equals("")) {
			//do nothing
		}
		else {
			//video enabled
			Master.sendBotsVideo();
		}
		
	}
	
	int botsConnected = 0;
	int nBots = 1;
	String server = new String("http://mconf.org:8888");
	String room = new String("");
	bot[] botArmy;
	int botIndex = 0;
	String videoFileName = new String("");
	
	public int getnBots() {	return nBots;	}
	public String getServer() {	return server;	}
	public String getRoom() {	return room;	}
	private String getVideoFileName() {	return videoFileName;	}
			
	//processes the arguments
	public BbbBot(String[] args){
		int i=0;
		for (i=0;i < args.length; i++) {
			
			if (args[i].equals("-n")) {
				nBots = Integer.parseInt(args[i+1]);			}
			
			if (args[i].equals("-m")) {
				room = args[i+1];			}
			
			if (args[i].equals("-s")) {
				server = args[i+1];			}
			
			if (args[i].equals("--meetings")){
				BigBlueButtonClient client = new BigBlueButtonClient();
				client.createJoinService(server);
				client.getJoinService().load();
				List<Meeting> onlineMeetings = client.getJoinService().getMeetings();
				System.out.println("\n\n\nOnline Meetings:");
				int k;
				for (k=onlineMeetings.size()-1; k>=0; k--)
				{
					System.out.println("MeetingID: " + "\"" + onlineMeetings.get(k).getMeetingID() + "\"" + ", "
							+ onlineMeetings.get(k).getParticipantCount() +  " participants"
							);
				}
				System.out.println("\n");
				System.exit(2);
			}
			
			if (args[i].equals("-v")){ 
				this.videoFileName = args[i+1]; 
			}
			
			if (args[i].equals("--help")) {
				System.out.println("BigBlueButtonBot commands:" +
						"\n-n [number_of_bots], default is "+ nBots +
						"\n-m [\"meeting_ID\"], choose the meeting(ID) to spawn the bots" +
						"\n-s [\"server_address\"], default is " + server +
						"\n-v [\"video_file_name\"], specify video to be sent, default is not to send video" +
						"\n--meetings, display current meetings(ID) and participant count" +
						"\nDeveloped for Mconf.");
				System.exit(1);
			}
						
		}
	}
		
	public void spawnBots(){
		botIndex = 0;
		botArmy = new bot[nBots];
		while (nBots > botIndex)
		{
			botArmy[botIndex] = new bot(server, room, videoFileName);
			if (botArmy[botIndex].connect(botIndex)) {
				botsConnected++;
			}
			botIndex++;
		}
		
		/*
		 *
		 * debugging:
		botIndex = 0;
		while (nBots > botIndex)
		{
			System.out.println("userId bot["+botIndex+"]: " + botArmy[botIndex].getMyUserId());
			botIndex++;
		}
		System.out.println("nBots: " + nBots );
		System.out.println("botsConnected: " + botsConnected);
		System.exit(21); */
		
	}
	
	
	public void sendBotsVideo(){
		botIndex = 0;
		while (nBots > botIndex +1)
		{
			botArmy[botIndex].sendVideo();
			botIndex++;
		}
	}

	
	
	
	
	
	
	
	/*
	
	
	
	
	private void sendBotsVideo(){
	
			IContainer container = IContainer.make();
			if(container.open(videoFileName, IContainer.Type.READ, null) < 0 ) {
				System.out.println("Sorry, could not read file.");
				System.exit(2);
			}
			
			int numStreams = container.getNumStreams();
			System.out.println("Streams: " + numStreams);
			
			int videoStreamID = -1;
			IStreamCoder videoCoder = null;
			
			for(int i=0;i<numStreams;i++) {
				
				IStream stream = container.getStream(i);
				IStreamCoder coder = stream.getStreamCoder();
				
				System.out.println("Stream "+i+": "+coder.getCodecType());
				System.out.println("codec: " + coder.getCodecID());
				
				if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
					videoStreamID = i;
					videoCoder = coder;
				}
			}
			
			if (videoStreamID == -1) {
				System.out.println("Sorry, there are no video streams on this file.");
				System.exit(3);	
			}
			
			if (videoCoder.open() < 0) {
				System.out.println("Sorry, failed to open video coder.");
				System.exit(4);
			}
			
			IPacket packet = IPacket.make();
			
			//for (int j=0; j < nBots; j++) {
				this.startPublisher(BotArmy[0], videoCoder.getWidth(), videoCoder.getHeight());
			//}
			
			while(container.readNextPacket(packet) >= 0) {
				
				if (packet.getStreamIndex() == videoStreamID) {
					//video packet
					IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
							videoCoder.getWidth(),
							videoCoder.getHeight()
							);
					
					//for(int loop=0; loop<10; loop++) {
						System.out.println("pacotinho de vídeo");
					//}
								
					//copy frame to sharedBuffer (?)
					sharedBuffer = picture.getData().getByteArray(0, picture.getSize()); //(offset, length)
					//sharedBuffer = new byte[200];
					long timeStamp =  picture.getTimeStamp();
					this.onReadyFrame(sharedBuffer.length, (int)timeStamp, BotArmy[0]);
					
				}
				else {
					//not a video packet
				}
				
			}
			
			if (videoCoder != null) {
				videoCoder.close();
				videoCoder = null;			}
			
			if (container != null) {
				container.close();
				container = null;			}
			
	}
	
	
	//From mconf-mobile,
	//org.mconf.android.core.video,
	//VideoPublish.java:
	
	int firstTimeStamp;
	int lastTimeStamp;
	byte[] sharedBuffer;
	private VideoPublishHandler videoPublishHandler;
	private List<Video> framesList = new ArrayList<Video>();
	private boolean framesListAvailable = false; 
	private boolean firstFrameWrote = false;
	private class VideoPublishHandler extends IVideoPublishListener {
		public VideoPublishHandler(int userId, String streamName, RtmpReader reader, BigBlueButtonClient context) {			
			super(userId, streamName, reader, context);
		}
	}
	
	public void startPublisher(BigBlueButtonClient context, int width, int height){
    	videoPublishHandler = new VideoPublishHandler(context.getMyUserId(),
    			width+"x"+height+context.getMyUserId(),
    			 this,
    			context);
    	videoPublishHandler.start();
    }
	
	public int onReadyFrame (int bufferSize, int timeStamp, BigBlueButtonClient context)
    {    	
				
		if(firstTimeStamp == 0){
    		firstTimeStamp = timeStamp;
    	}    	
    	timeStamp = timeStamp - firstTimeStamp;
    	int interval = timeStamp - lastTimeStamp;
    	lastTimeStamp = timeStamp;
    	
    	byte[] aux = new byte[bufferSize];
    	System.arraycopy(sharedBuffer, 0, aux, 0, bufferSize);
    	    	
       	Video video = new Video(timeStamp, aux, bufferSize);
   	    video.getHeader().setDeltaTime(interval);
   	    
   	    //System.out.println(videoPublishHandler.videoConnection.streamId);
   	    //
   	    System.out.println(context.getUsersModule().getParticipants());
   	    System.exit(6);
   	    
		video.getHeader().setStreamId(videoPublishHandler.videoConnection.streamId);
		
   	 	System.out.println("on Ready Frame");
   	   	    
		if(context.getUsersModule().getParticipants().get(context.getMyUserId()).getStatus().isHasStream() &&
		    framesListAvailable && framesList != null){
			framesList.add(video);
			if(!firstFrameWrote){
				if(videoPublishHandler != null && videoPublishHandler.videoConnection != null 
						&& videoPublishHandler.videoConnection.publisher != null 
						&& videoPublishHandler.videoConnection.publisher.isStarted()) 
				{
					firstFrameWrote = true;
					videoPublishHandler.videoConnection.publisher.fireNext(
							videoPublishHandler.videoConnection.publisher.channel, 0);
					System.exit(7);
					 
				} 
				else {
					//log.debug("Warning: tried to fireNext but video publisher is not started");
					System.out.println("tried to fireNext but video publisher is not started");
				}
			}
			synchronized(this) {
				this.notifyAll();
			}
		}
		
    	return 0;
    }
	
	@Override
	public void close() {		
		framesListAvailable = false;
		if(framesList != null){
			framesList.clear();
		}
		framesList = null;
	}

	@Override
	public Metadata getMetadata() {
		return null;
	}

	@Override
	public Video[] getStartMessages() {
		framesListAvailable = true;
		Video[] startMessages = new Video[0];
        return startMessages;
	}

	@Override
	public long getTimePosition() {
		return 0;
	}

	@Override
	public boolean hasNext() {
		if(framesListAvailable && framesList != null && framesList.isEmpty()){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(framesListAvailable && framesList != null){ // means that the framesList is not empty
			return true;
		} else { // means that the framesList is empty or we should not get next frames
			return false;
		}
	}

	@Override
	public Video next() {
		if(framesListAvailable && framesList != null){
			return framesList.remove(0);
		} else {
			Video emptyVideo = new Video();
	        return emptyVideo;
		}
	}

	@Override
	public long seek(long timePosition) {
		return 0;
	}

	@Override
	public void setAggregateDuration(int targetDuration) {
	}


*/

}


