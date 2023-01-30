package source;

public class Main {
	
	public static void main (String[] args)
	{
		System.out.print("THIS IS THE MAIN BRANCH");
		
		//THIS IS TO TEST FLOOR DELETE WHEN MERGING
		Scheduler s = new Scheduler();
		Floor f = new Floor(s);
		
		f.run();
	}

}
