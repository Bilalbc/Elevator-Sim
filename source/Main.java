package source;

public class Main {
	
	public static void main (String[] args)
	{		
		Scheduler s = new Scheduler();
		
		Thread f = new Thread(new Floor(s), "Floor");
		Thread e = new Thread(new Elevator(s), "Elevator");
		
		f.start();
		e.start();
	}

}
