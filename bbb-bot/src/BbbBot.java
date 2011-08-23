import org.mconf.bbb.BigBlueButtonClient;

public class BbbBot {
	int nBots = 1;
	String server = new String("http://mconfweb.inf.ufrgs.br");
	String room = new String("");
	BigBlueButtonClient[] BotArmy;
	int Army_index = 0;
	
	public static void main (String[] args) {
		BbbBot Master = new BbbBot(args);
		int botsCreated = Master.spawnBots();
		System.out.println(botsCreated + " bots created @ " + Master.getRoom() + " @ " + Master.getServer());
	}
	
	public BbbBot(String[] args){
		int j=0;
		int i=0;
		for (i=0;i < args.length; i++) {
			if (args[i].equals("-n")) {
				nBots = Integer.parseInt(args[i+1]);
				BotArmy = new BigBlueButtonClient[nBots];
				do{
					BotArmy[j] = new BigBlueButtonClient();
					j++;
				}while(j<nBots);
			}
			if (args[i].equals("-r")) {
				room = args[i+1];
			}
			if (args[i].equals("-s")) {
				server = args[i+1];
			}			
		}
	}
			
	private int spawnBots(){
		int spawnned = 0;
		Army_index = 0;
		while (nBots > 0)
		{
			String name = "HappyBot#" + Integer.toString(Army_index);
			//BotArmy[Army_index] = new BigBlueButtonClient();
			BotArmy[Army_index].getJoinService().load(this.server);
			BotArmy[Army_index].getJoinService().join(this.room, name, true);
			if (BotArmy[Army_index].getJoinService().getJoinedMeeting() != null) 
			{
				BotArmy[Army_index].connectBigBlueButton();
				System.out.println("return true");
				Army_index++;
				spawnned++;
			}
			else
			{
				System.out.println("return false");
			}
		nBots--;
		}
		
		return spawnned;
		/*
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
		*/
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
