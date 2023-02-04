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
 * @Author Bilal Chaudhry 101141634
 *
 * @Date: 2023-02-04
 * @Version 1.0
 *
 *
 *  Test class for Iteration 1
 */

public class Iteration1Test {

	private File testData;

	@Before
	public void setUp() {
		this.testData = new File("src//source//Requests.csv");
	}

	@Test
	public void testPassAndRecieveRequest() {
		Scheduler s = new Scheduler();
		Thread f = new Thread(new Floor(s), "Floor");
		Thread e = new Thread(new Elevator(s), "Elevator");

		f.start();
		e.start();

		while (f.isAlive() || e.isAlive()) {
			// Busy waiting
		}

		assertTrue(s.getMessagesRecieved() == 2);
		assertTrue(s.getRepliesRecieved() == 2);

		s.setClosed();
	}

	@Test
	public void testPassMessage() {
		Message expectedRequest;
		Scheduler s = new Scheduler();

		try {
			Scanner scanner = new Scanner(testData);

			while (scanner.hasNext()) {
				expectedRequest = createRequest(scanner.nextLine());
				s.passMessage(expectedRequest);
				assertTrue(s.readMessage().equals(expectedRequest));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPassReply() {
		Message expectedResponse;
		Scheduler s = new Scheduler();

		try {
			Scanner scanner = new Scanner(testData);

			Thread e = new Thread(new Elevator(s), "Elevator");
			e.start();

			while (scanner.hasNext()) {
				expectedResponse = createRequest(scanner.nextLine());
				s.passMessage(expectedResponse);
				assertTrue(s.readReply().equals(expectedResponse));
			}
			scanner.close();
			s.setClosed();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private Message createRequest(String request) {
		String[] values = request.split(", ");
		Message req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));

		return req;
	}
}
