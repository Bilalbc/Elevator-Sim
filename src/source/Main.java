package source;

public class Main {
	
	public static void main (String[] args)
	{
		
		//THIS IS TO TEST FLOOR DELETE WHEN MERGING
		Scheduler s = new Scheduler();
		Floor f = new Floor(s);
		
		f.run();
	}

}
