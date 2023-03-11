package source;

import java.io.File;

/** 
 * @author Bilal Chaudhry
 * @version 2.0
 * @date February 27th, 2023
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

		File file = new File("src//source//Requests.csv");
		Thread f = new Thread(new Floor(file), "Floor");
		Thread floorHandler = new Thread(new FloorHandler(s), "FloorHandler");
		
		Thread e1 = new Thread(new Elevator (69, 1), "0");
		Thread e2 = new Thread(new Elevator (70, 2), "1");
		Thread e3 = new Thread(new Elevator (71, 3), "2");
		Thread e4 = new Thread(new Elevator (72, 4), "3");
		Thread eh1 = new Thread(new ElevatorHandler(s, 69), "0");
		Thread eh2 = new Thread(new ElevatorHandler(s, 70), "1");
		Thread eh3 = new Thread(new ElevatorHandler(s, 71), "2");
		Thread eh4 = new Thread(new ElevatorHandler(s, 72), "3");

		f.start();
		e1.start();
		e2.start();
		e3.start();
		e4.start();
		
		floorHandler.start();
		eh1.start();
		eh2.start();
		eh3.start();
		eh4.start();
		
	}

}
