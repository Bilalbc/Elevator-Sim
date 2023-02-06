package source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
/**
 * Floor object which reads instructions from file and passes it to the elevator scheduler
 * 
 * @author Kousha Motazedian, Matthew Parker
 * @version 1.0
 * @date February 4th, 2023
 */
public class Floor implements Runnable{
	private Scheduler scheduler;
	
	/**
	 * Constructor for Floor class
	 * @param sch, Scheduler being used
	 */
	public Floor(Scheduler sch) {
		this.scheduler = sch;
	}
	
	/**
	 * Method to create a request message by reading file
	 * 
	 * @param reader, File reader being used
	 * @return req, Request information, from file
	 */
	private Message createRequest(Scanner reader) {

		String data = reader.nextLine();
		String[] values = data.split(", ");
		Message req = new Message(values[0],  Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));
		
		return req;
	}
	
	@Override
	/**
	 * Run method for Floor class,
	 * Scans file using reader, and checks for instructions, and if there are, that is passed in a message
	 * to the scheduler, and then the reply from the elevator is printed
	 */
	public void run() {
		try {
			File file = new File("src//source//Requests.csv");
			Scanner reader = new Scanner(file);
			while(reader.hasNextLine()) {
				Message req = createRequest(reader);
				System.out.println("Passing message: " + req);
				scheduler.passMessage(req);
				System.out.println("Reading reply: " + scheduler.readReply().getReturnMessage());
				
				Thread.sleep(500);
			}
			
			scheduler.setClosed();
			reader.close();
		} catch (FileNotFoundException | InterruptedException e) {}
	}
	
}
