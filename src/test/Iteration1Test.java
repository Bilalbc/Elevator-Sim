package test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import source.Elevator;
import source.Floor;
import source.Message;
import source.Scheduler;

/**
 * @Author Bilal Chaudhry
 *
 * @Date: 2023-02-04
 * @Version 1.0
 *
 *
 * Test class for Iteration 1
 * 
 * Tests both ends of communication with the scheduler, manually passing data 
 * for a thread to receive, and full communication loop, verifying the amount 
 * of Messages passed.
 * 
 */


public class Iteration1Test {

	private File testData;

	/**
	 * Execute before other test cases
	 * 
	 * Setup testData file location File contains two lines (requests)
	 */
	@Before
	public void setUp() {
		this.testData = new File("src//source//Requests.csv");
	}

	/**
	 * Test end to end communication, asserting that the correct number of Messages
	 * were sent by Floor thread and received by Elevator thread
	 * 
	 * This test is not concerned with contents of Messages, only their delivery.
	 * Contents are tested in proceeding test cases
	 * 
	 * @result test passes if Scheduler received two Messages from the Floor Thread
	 *         and two replies from Elevator Thread
	 */
	@Test
	public void testPassAndRecieveRequest() {

		System.out.println("-=-=-=-=-= testPassAndRecieveRequest =-=-=-=-=-=-");

		// New scheduler for each test case to avoid interference
		Scheduler s = new Scheduler();

		Thread f = new Thread(new Floor(s), "Floor");
		Thread e = new Thread(new Elevator(s), "Elevator");

		f.start();
		e.start();

		// allow both threads to finish execution
		while (f.isAlive() || e.isAlive()) {
			// Busy waiting
		}

		// Two messages and replies should be tracked by Scheduler
		assertTrue(s.getMessagesRecieved() == 2);
		assertTrue(s.getRepliesRecieved() == 2);

		s.setClosed();
	}

	/**
	 * Test contents of Message from both ends of the Scheduler, simulating
	 * receiving a Message from Floor thread and sending the Message to Elevator
	 * thread
	 * 
	 * @result Test passes if the contents retrieved from Scheduler of both Messages
	 *         match the data pulled from testData file
	 */
	@Test
	public void testPassMessage() {

		System.out.println("-=-=-=-=-= testPassMessage =-=-=-=-=-=-");
		// New scheduler for each test case to avoid interference
		Scheduler s = new Scheduler();

		try {
			Scanner scanner = new Scanner(testData);

			// test each request within file
			while (scanner.hasNext()) {
				// create Message from file
				Message expectedRequest = createRequest(scanner.nextLine());

				// Simulate Floor thread passing Message
				s.passMessage(expectedRequest);

				// Simulate Elevator reading Message, assert Message is same as was passed in
				assertTrue(s.readMessage().equals(expectedRequest));
			}
			scanner.close();
			s.setClosed();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test full Message loop through the Scheduler, simulating a Message being sent
	 * from Floor thread to the scheduler, and the Floor thread reading the reply
	 * Message sent back from the Elevator thread.
	 * 
	 * @result Test passes if the Message contents retrieved from the Elevator reply
	 *         are the same as the simulated message passed in from floor.
	 */
	@Test
	public void testPassReply() {

		System.out.println("-=-=-=-=-= testPassReply =-=-=-=-=-=-");

		// New scheduler for each test case to avoid interference
		Scheduler s = new Scheduler();

		try {
			Scanner scanner = new Scanner(testData);

			// initialize elevator thread to process simulated Messages sent from Floor
			Thread e = new Thread(new Elevator(s), "Elevator");
			e.start();

			// test each line within the file
			while (scanner.hasNext()) {
				// create message from file
				Message expectedResponse = createRequest(scanner.nextLine());
				// simulate Floor passing message to Scheduler
				s.passMessage(expectedResponse);
				// simulate Floor reading the response from Elevator
				assertTrue(s.readReply().equals(expectedResponse));
			}
			scanner.close();
			s.setClosed();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Helper method to create a Message from file contents read by scanner
	 * 
	 * @param request of type String to create a Message
	 * @return Message created from request
	 */
	private Message createRequest(String request) {
		String[] values = request.split(", ");
		Message req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));

		return req;
	}
}
