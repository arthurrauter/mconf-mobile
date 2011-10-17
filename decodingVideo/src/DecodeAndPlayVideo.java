import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.demos.VideoImage;



public class DecodeAndPlayVideo {

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("Please, inform filename.");
			System.exit(1);
		}
		
		String filename = args[0];
		
		IContainer container = IContainer.make();
		
		if(container.open(filename, IContainer.Type.READ, null) < 0 ) {
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
		
		int numOfPackets =0;
		
		//OpenJavaWindow();
		
		IPacket packet = IPacket.make();
		while(container.readNextPacket(packet) >= 0) {
			
			if (packet.getStreamIndex() == videoStreamID) {
				System.out.println("Video Packet here");
				numOfPackets++;
				//video packet
				
				IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
						videoCoder.getWidth(),
						videoCoder.getHeight()
						);
				
				byte[] frame = picture.getData().getByteArray(0, 128);
				
				
				/*
				int offset=0;
				while(offset <packet.getSize()) {
					
					int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
					if (bytesDecoded < 0) {
						System.out.println("could not decode video");
						System.exit(5);
					}
					offset += bytesDecoded;
					
					if (picture.isComplete()) {
						
					}
					
						
				}
				*/
				
			}
			else {
				//not a video packet
			}
			
		}
		
		System.out.println("Packets of video:"+numOfPackets);
		
		if (videoCoder != null) {
			videoCoder.close();
			videoCoder = null;
		}
		
		if (container != null) {
			container.close();
			container = null;
		}
		
		System.out.println("end");
		
		//CloseJavaWindow();
		
		
		
		
		

	}
	
	private static VideoImage mScreen = null;

}
