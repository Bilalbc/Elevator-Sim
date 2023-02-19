package source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Floor object which reads instructions from file and passes it to the elevator
 * scheduler
 * 
 * @author Kousha Motazedian, Matthew Parker
 * @version 2.0
 * @date February 27th, 2023
 */
public class Floor implements Runnable {
	private Scheduler scheduler;
	private File floorRequests;
	private int messageDelay = 0;

	/**
	 * Constructor for Floor class
	 * 
	 * @param sch, Scheduler being used
	 */
	public Floor(Scheduler sch, File file) {
		this.scheduler = sch;
		this.floorRequests = file;
	}

	/**
	 * Method to create a request message by reading file validates message before
	 * returning
	 * 
	 * @param reader, File reader being used
	 * @return req, Request information, from file null if data was in incorrect
	 *         format
	 */
	private Message createRequest(Scanner reader) {

		String data = reader.nextLine();
		String[] values = data.split(", ");
		Message req = null;

		if (validateRequest(values)) {
			req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));
			messageDelay = Integer.parseInt(values[4]);
		}
		return req;

	}

	/**
	 * Method used to validate the contents of request data. Data must follow format
	 * similar to: 13:02, 2, UP, 4, 500
	 * 
	 * @param request
	 * @return boolean : true if request follows correct format, and false otherwise
	 */
	private boolean validateRequest(String[] request) {

		if (request.length == 5) {
			if (request[0].matches("\\d+:\\d+") && 
					request[1].matches("\\d+") && 
					request[2].equals("UP") || request[2].equals("DOWN") && 
					request[3].matches("\\d+") && 
					request[4].matches("\\d+")) {
				return true;
			}
		}
		System.err.println("An incorrect request was recieved");
		return false;
	}

	@Override
	/**
	 * Run method for Floor class, Scans file using reader, and checks for
	 * instructions, and if there are, that is passed in a message to the scheduler,
	 * and then the reply from the elevator is printed
	 */
	public void run() {
		try {
			Scanner reader = new Scanner(floorRequests);
			while (!scheduler.isClosed()) {
				if (reader.hasNextLine()) {

					// if the delay to send the request has elapsed
					if (messageDelay <= 0) {
						Message req = createRequest(reader);
						if (req != null) {
							System.out.println("Passing message: " + req);
							scheduler.passMessage(req);
						}
					} 
					else {
						// delay by part of total delay to allow floor to continue to receive replies
						// from the elevator
						messageDelay -= 100;
						Thread.sleep(10);
					}
				} 
				else {
					// notify scheduler that there are no more requests to be sent
					scheduler.setRequestsComplete(true);
				}
				Message returned = scheduler.readReply();
				System.out.println(returned.getReturnMessage());
				Thread.sleep(500);

			}
			reader.close();
		} catch (FileNotFoundException | InterruptedException e) {
			System.out.println("filenotfound");
		}
	}
}
