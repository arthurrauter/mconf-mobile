//BigBlueButtonBot, GT-MCONF @PRAV-UFRGS, developed by Arthur C. Rauter, august 2011.
//Adding video to the bots through Xuggler library, September 2011.


import java.util.List;

import org.mconf.bbb.BigBlueButtonClient;
import org.mconf.bbb.api.Meeting;

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
	private int botIndex = 0;
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
	
	//Only usable for one bot, atm.
	//For more bots:
	//Should change the logic of sendBotsVideo to open the video,
	//update all the bots 'sharedBuffer' variable and then send from each bot.
	public void sendBotsVideo(){
		botIndex = 0;
		
		//This loop is intended to make the process wait for the bots to connect.
		//This loop works, but may be alot more elegant of you to just use the onConnected() function,
		//which is in bot.java. GLHF.
		int i=0;
		while(i<2000000){
			System.out.println("waiting for bots");
			i++;
		}
		
		
		int failedBots = 0;
		while (nBots > botIndex)
		{
			//System.out.println("userId bot["+botIndex+"]: " + botArmy[botIndex].getMyUserId());
			if (botArmy[botIndex].getMyUserId() < 0)
				failedBots++;
			botIndex++;
		}
		
		if (failedBots == nBots)
		{
			//System.out.println("nBots: " + nBots );
			//System.out.println("botsConnected: " + botsConnected);
			System.out.println("All connections failed, please, try again.");
			System.exit(404);
		}
		System.out.println(failedBots);
		botIndex = 0;
		while (nBots > botIndex + failedBots)
		{
			botArmy[botIndex].sendVideo();
			botIndex++;
		}
	}
}


