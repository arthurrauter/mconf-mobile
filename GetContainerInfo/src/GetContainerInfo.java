import com.xuggle.mediatool.event.ICoderEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;


public class GetContainerInfo {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("argument error.");
			System.exit(1);
		}
		IContainer container = IContainer.make();
		
		if (container.open(args[0], IContainer.Type.READ, null) < 0) {
			System.out.println("could not open file.");
		}
		else	
			System.out.println("opened file.");
			
		int numStreams = container.getNumStreams();
		System.out.println("Streams: " + numStreams);
		
		for(int i=0; i < numStreams; i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			
			System.out.println("Stream "+i+": "+coder.getCodecType());
			System.out.println("codec: " + coder.getCodecID());
		}
		
		int j;
		int audioStreamID = -1;
		for (j=0;j<numStreams; j++)	{
			IStream stream = container.getStream(j);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				System.out.println("AUDIO!");
				audioStreamID = j;
				break;
			}
		}
		
		IStreamCoder audioCoder = container.getStream(audioStreamID).getStreamCoder();
		
		if(audioCoder.open() < 0)
			System.out.println("could not open audioCoder");
		
		IPacket packet = IPacket.make();
		
		int numPackets = 0;
		
		while(container.readNextPacket(packet) >= 0)
		{
			if (packet.getStreamIndex() == audioStreamID) {
				System.out.println("audio packet read!");
				numPackets++;
			}
			else
				System.out.println("packet read!");
		}
		
		//To actually have sound, there's more to do than just open the packets. References at xuggle tutorials.
		
		System.out.println("audio packets: "+numPackets);
		System.out.printf("file size (bytes): %d; ", container.getFileSize());
			
	}
	
}


