import org.mconf.bbb.BigBlueButtonClient;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;


public class bot extends BigBlueButtonClient{
	
	String name = new String("BOT");
	String server = new String("http://mconf.org:8888");
	String room = new String("");	
	String videoFileName = new String("");
	botVideoPublish botVideoPublish;
	

	public String getServer() { return server;	}
	public String getRoom() { return room;	}
	public String getVideoFileName() { return videoFileName;	}
	
	public bot(String server, String room, String videoFileName){
		this.server = server;
		this.room = room;
		this.videoFileName = videoFileName;
	}
		
	public boolean connect(int number){
		name = name + Integer.toString(number);
		this.createJoinService(server);
		this.getJoinService().load();
		this.getJoinService().join(room, name, true);
		if (this.getJoinService().getJoinedMeeting() != null) 
		{
			if(this.connectBigBlueButton())
				return true;
			else 
				return false;
		}
		else
		{
			System.out.println(name  + " failed to join the meeting");
			System.exit(3);
			return false;
		}
	}

	public void sendVideo() {
		IContainer container = IContainer.make();
			
		if(container.open(videoFileName, IContainer.Type.READ, null) < 0 ) {
			System.out.println("Sorry, could not read file.");
			System.exit(2);
		}
		
		int numStreams = container.getNumStreams();
		int videoStreamID = -1;
		IStreamCoder videoCoder = null;
		for(int i=0;i<numStreams;i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
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
		
		double frameRate = videoCoder.getFrameRate().getDouble();
		botVideoPublish = new botVideoPublish(this, true, (int)frameRate , videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getBitRate()); 
		botVideoPublish.startPublisher();
		IPacket packet = IPacket.make();
		
		while(container.readNextPacket(packet) >= 0) {
			if (packet.getStreamIndex() == videoStreamID) {
				
				IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
						videoCoder.getWidth(),
						videoCoder.getHeight()
						);
				byte[] sharedBuffer = picture.getData().getByteArray(0, picture.getSize()); //(offset, length)
				long timeStamp =  picture.getTimeStamp();
				this.botVideoPublish.onReadyFrame(sharedBuffer.length, (int)timeStamp, sharedBuffer);
			}
			else {
				//not a video packet
			}
	
		}
	
	
	}
}
