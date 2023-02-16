package source;

/** 
 * @author Bilal Chaudhry
 * @version 2.0
 * 
 * Main Class that has the main method to run the program.
 *
 */
public class Main {

	/**
	 * The main method
	 * 
	 * @param args, String[]
	 */

	public static void main(String[] args) {
		Scheduler s = new Scheduler();

		Thread f = new Thread(new Floor(s), "Floor");
		Thread e = new Thread(new Elevator(s), "Elevator");

		f.start();
		e.start();
	}

}
