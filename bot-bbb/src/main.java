
public class main {
	
		
	public static void main (String[] args) {
		// TODO Auto-generated method stub
		int i;
		int people = 0;
		for (i=0;i < args.length; i++)
		{
			if (args[i].equals("-n"))
			{
				people = Integer.parseInt(args[i+1]);
			}
			
		}
		System.out.println("we got n people wher n equals to " + people);
		
		
		
	}
}
