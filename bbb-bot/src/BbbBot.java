import org.mconf.bbb.BigBlueButtonClient;

public class BbbBot {
	
	int nBots = 1;
	String server = new String("http://mconfweb.inf.ufrgs.br");
	String room = new String("");
	BigBlueButtonClient[] BotArmy;
	int Army_index = 0;
	int Array[] = new int[12];
	
	public void arrayout(){
		int i;
		for (i=0;i<Array.length;i++){
			System.out.println(Array[i]);
		}
	}
	
	public static void main (String[] args) {
		int botCounter = 0;
		int botsCreated = 0;
		BbbBot Master = new BbbBot(args);
		Master.Array[1] = 1;
		Master.Array[5] = 12;
		Master.arrayout();
		
		
		for (botCounter=Master.getnBots(); botCounter > 0; botCounter--) {
			if (true)//Master.makeBot())
			{
				botsCreated++;
			}
		}
		System.out.println(botsCreated + " bots created @ " + Master.getRoom() + " @ " + Master.getServer());
	}
	
	public BbbBot(String[] args){
		int i = 0;
		for (i=0;i < args.length; i++) {
			if (args[i].equals("-n")) {
				nBots = Integer.parseInt(args[i+1]);
				BotArmy = new BigBlueButtonClient[nBots];
			}
			if (args[i].equals("-r")) {
				room = args[i+1];
			}
			if (args[i].equals("-s")) {
				server = args[i+1];
			}			
		}
	}
			
	private boolean makeBot(){
		String name = "HappyBot#" + Integer.toString(Army_index);
		/*BotArmy[Army_index] = new BigBlueButtonClient();
		BotArmy[Army_index].getJoinService().load(this.server);
		BotArmy[Army_index].getJoinService().join(this.room, name , true);
		if (BotArmy[Army_index].getJoinService().getJoinedMeeting() != null) {
			BotArmy[Army_index].connectBigBlueButton();
			System.out.println("return true");
			Army_index++;
			return true;
		}
		else
		{
			System.out.println("return false");
			return false;
		}*/
		
		BigBlueButtonClient client = new BigBlueButtonClient();
		client.getJoinService().load(this.server);
		client.getJoinService().join(this.room, name , true);
		if (client.getJoinService().getJoinedMeeting() != null) {
			client.connectBigBlueButton();
			System.out.println("return true");
			Army_index++;
			return true;
		}
		else
		{
			System.out.println("return false");
			return false;
		}
		
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
}
