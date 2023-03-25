package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import source.Elevator;
import source.ElevatorHandler;
import source.Floor;
import source.FloorHandler;
import source.Message;
import source.Scheduler;

/**
 * @author Bilal Chaudhry
 * @Date: 2023-03-11
 * @Version 3.0
 * 
 *          Unit test class for Elevator-sim
 * 
 *          Tests states, and events for Elevator and Scheduler, and the passing
 *          of messages between the three classes
 *
 */
public class Iteration3Test {

	private File testFile;
	private File algorithmTestFile;

	/**
	 * Execute before test cases Setup testFile containing one entry of a proper
	 * request, and a badTestFile containing one entry of an improper request.
	 */
	@Before
	public void setUp() {
		this.testFile = new File("src//test//testData.csv");
		this.algorithmTestFile = new File("src//test//AlgorithmTest.csv");
	}

	/**
	 * Test case for ports between Floor and FloorHandler. tests by sending a
	 * simulated message from the floor class to the handler, and then confirms the
	 * message that is saved in the scheduler matches.
	 * 
	 * @result test passes if the message in the scheduler matches the event that
	 *         was originally sent
	 */
	@Test
	public void testFloorPorts() {

		System.out.println("---------------");
		Scheduler sch = new Scheduler();
		Floor f = new Floor(testFile);
		FloorHandler fh = new FloorHandler(sch);

		Thread ft = new Thread(f);
		Thread fth = new Thread(fh);

		Message m = new Message("13:02", 1, "UP", 2);

		ft.start();
		fth.start();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(m.equals(sch.getMessages().get(0)));
	}

	/**
	 * Test case for ports between Elevator and ElevatorHandler, confirms if the
	 * reply sent from an elevator is the same as what is stored in the scheduler as
	 * a reply
	 * 
	 * @result Test passes if the contents of the reply in Scheduler is the same as
	 *         what was sent from elevator
	 */
	@Test
	public void testElevatorPorts() {

		System.out.println("---------------");

		Scheduler sch = new Scheduler();
		Elevator ev = new Elevator(68, 1);
		ElevatorHandler eh = new ElevatorHandler(sch, 68);

		Thread et = new Thread(ev);
		Thread eth = new Thread(eh, "0");

		Message m = new Message("Elevator 1: is on floor 1 and is DOORSCLOSED");

		et.start();
		eth.start();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(m.getReturnMessage().equals(sch.getReplies().get(0).getReturnMessage()));
	}

	/**
	 * Test case for the Scheduler Algorithm, tests to see if the algorithm properly
	 * distributes the requests as efficiently as possible
	 * 
	 */
	@Test
	public void testSchedulerAlgorithm() {
		Scheduler sch = new Scheduler();

		Thread e1 = new Thread(new Elevator(69, 1), "0");
		Thread e2 = new Thread(new Elevator(70, 2), "1");
		Thread e3 = new Thread(new Elevator(71, 3), "2");
		Thread e4 = new Thread(new Elevator(72, 4), "3");

		Thread eh1 = new Thread(new ElevatorHandler(sch, 69), "0");
		Thread eh2 = new Thread(new ElevatorHandler(sch, 70), "1");
		Thread eh3 = new Thread(new ElevatorHandler(sch, 71), "2");
		Thread eh4 = new Thread(new ElevatorHandler(sch, 72), "3");

		e1.start();
//		e2.start();
//		e3.start();
//		e4.start();
		eh1.start();
//		eh2.start();
//		eh3.start();
//		eh4.start();

		try {
			Scanner reader = new Scanner(algorithmTestFile);
			
			while (reader.hasNextLine()) {
				sch.passMessage(createRequest(reader.nextLine()));

				sch.schedulerAlgorithm();
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (!e1.isInterrupted()) {
			try {
				Thread.sleep(500);

				System.out.println(sch.getElevatorQueue().get(0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Helper method to create a Message from file contents read by scanner
	 * 
	 * @return Message created from request
	 */
	private Message createRequest(String request) {
		Message req = null;
		String[] values = request.split(", ");
		req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));

		try {
			Thread.sleep(Integer.parseInt(values[4]));
		} catch (NumberFormatException | InterruptedException e) {
			e.printStackTrace();
		}

		return req;
	}
}
