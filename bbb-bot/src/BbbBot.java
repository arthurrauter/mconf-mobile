//BigBlueButtonBot, GT-MCONF @PRAV-UFRGS, developed by Arthur C. Rauter, august 2011.
//Adding video to the bots through Xuggler library, September 2011.

import java.util.ArrayList;
import java.util.List;

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


public class BbbBot {
	int nBots = 1;
	String server = new String("http://mconf.org:8888");
	String room = new String("");
	BigBlueButtonClient[] BotArmy;
	int Army_index = 0;
	String videoFileName = new String("");
	
	//Creates the Master, which will spawn the bots.
	public static void main (String[] args) {
		BbbBot Master = new BbbBot(args);
		int botsCreated = Master.spawnBots();
		System.out.println(botsCreated + " bots created @ " + Master.getRoom() + " @ " + Master.getServer());
		if (!Master.getVideoFileName().equals(""))
		{
			Master.sendBotsVideo();
		}
	}
	
	
	//processes the arguments and initializes the bot army
	public BbbBot(String[] args){
		int i=0;
		for (i=0;i < args.length; i++) {
			if (args[i].equals("-n")) {
				nBots = Integer.parseInt(args[i+1]);
			}
			
			if (args[i].equals("-m")) {
				room = args[i+1];
			}
			if (args[i].equals("-s")) {
				server = args[i+1];
			}
			
			if (args[i].equals("--meetings")){
				BigBlueButtonClient client = new BigBlueButtonClient();
				client.createJoinService(server);
				//client.getJoinService().setServer(server);
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
		int j=0;
		BotArmy = new BigBlueButtonClient[nBots];
		do{
			BotArmy[j] = new BigBlueButtonClient();
			
			j++;
		}while(j<nBots);
	}
			
	private int spawnBots(){
		int spawnned = 0;
		Army_index = 0;
		while (nBots > 0)
		{
			String name = "HappyBot#" + Integer.toString(Army_index);
			BotArmy[Army_index].createJoinService(server);
			BotArmy[Army_index].getJoinService().load();
			BotArmy[Army_index].getJoinService().join(this.room, name, true);
			if (BotArmy[Army_index].getJoinService().getJoinedMeeting() != null) 
			{
				BotArmy[Army_index].connectBigBlueButton();
				Army_index++;
				spawnned++;
			}
			else
			{
				System.out.println("Failed to join the meeting");
				System.exit(3);
			}
		nBots--;
		}
		
		return spawnned;
		
	}
	
	public int getnBots() {
		return nBots;
	}

	public String getServer() {
		return server;
	}

	public String getRoom() {
		return room;
	}
	
	private String getVideoFileName() {
		return videoFileName;
	}
	
	
	
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
			
			//int numOfPackets =0;
			//OpenJavaWindow();
			
			IPacket packet = IPacket.make();
			while(container.readNextPacket(packet) >= 0) {
				
				if (packet.getStreamIndex() == videoStreamID) {
					
					
					//System.out.println("Video Packet here");
					//numOfPackets++;
					
					//video packet
					
					IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
							videoCoder.getWidth(),
							videoCoder.getHeight()
							);
					
					//copy frame to sharedBuffer (?)
					sharedBuffer = picture.getData().getByteArray(0, picture.getSize()); //(offset, length)
					long timeStamp =  picture.getTimeStamp();
					this.onReadyFrame( (int)timeStamp, sharedBuffer.length, BotArmy[0]);
					
				}
				else {
					//not a video packet
				}
				
			}
			
			//System.out.println("Packets of video:"+numOfPackets);
			
			if (videoCoder != null) {
				videoCoder.close();
				videoCoder = null;
			}
			
			if (container != null) {
				container.close();
				container = null;
			}
			
			//System.out.println("end");
			//CloseJavaWindow();
	}
	
	
	//From mconf-mobile, org.mconf.android.core.video;VideoPublish.java
	
	int firstTimeStamp;
	int lastTimeStamp;
	byte[] sharedBuffer;
	private VideoPublishHandler videoPublishHandler;
	private List<Video> framesList = new ArrayList<Video>();
	//private static final Logger log = LoggerFactory.getLogger(VideoPublish.class);
	private boolean framesListAvailable = false; 
	private boolean firstFrameWrote = false;
	private class VideoPublishHandler extends IVideoPublishListener {
		public VideoPublishHandler(int userId, String streamName, RtmpReader reader, BigBlueButtonClient context) {			
			super(userId, streamName, reader, context);
		}
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
		video.getHeader().setStreamId(videoPublishHandler.videoConnection.streamId);
		
		if(context.getUsersModule().getParticipants().get(context.getMyUserId()).getStatus().isHasStream()
		   && framesListAvailable && framesList != null){
			framesList.add(video);
			if(!firstFrameWrote){
				if(videoPublishHandler != null && videoPublishHandler.videoConnection != null
						&& videoPublishHandler.videoConnection.publisher != null
						&& videoPublishHandler.videoConnection.publisher.isStarted()) {
					firstFrameWrote = true;
					videoPublishHandler.videoConnection.publisher.fireNext(
							videoPublishHandler.videoConnection.publisher.channel, 0);
				} else {
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




}


