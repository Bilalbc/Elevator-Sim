package test;

/**
 * @author Bilal Chaudhry
 * @Date: 2023-03-25
 * @Version 4.0
 * 
 *          Unit test class for Elevator-sim
 * 
 *          Tests failure states for Elevator and Scheduler, and if they are handled properly
 *
 */
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import source.Elevator;
import source.Elevator.ElevatorStates;
import source.ElevatorHandler;
import source.Message;
import source.Scheduler;

public class Iteration4Test {

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
	 * Test case for Elevator TIMEOUT error state. Tests by forcing the elevator
	 * thread to suspend to allow the TimeoutTimer to interrupt the Elevator thread.
	 * 
	 * @result test passes if the Elevator's state is set to TIMEOUT
	 */
	@Test
	public void testElevatorTimeoutHandling() {
		Scheduler sch = new Scheduler();
		Elevator ev = new Elevator(69, 1);

		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, 69), "0");

		e1.start();
		eh1.start();

		try {
			Scanner reader = new Scanner(algorithmTestFile);

			while (reader.hasNextLine()) {
				sch.passMessage(createRequest(reader.nextLine()));

				sch.schedulerAlgorithm();
			}

			reader.close();

			// Force the Timeout Timer to interrupt the Elevator
			e1.suspend();
			Thread.sleep(2000);
			e1.resume();

			// Wait until the state is set to TIMEOUT
			while (ev.getcurrentState() != ElevatorStates.TIMEOUT) {
				Thread.sleep(100);
			}
			assertTrue(ev.getcurrentState() == ElevatorStates.TIMEOUT);

		} catch (InterruptedException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test case for Elevator DOORSSTUCK states. tests by setting the state to
	 * STUCKOPEN or STUCKCLOSED, and verifying the error is handled gracefully
	 * 
	 * @result Test passes if doors are set to correct Error State and then righted
	 *         automatically
	 */
	@Test
	public void testDoorsStuckHandling() {
		Elevator elevator1 = new Elevator(69, 1);
		Elevator elevator2 = new Elevator(70, 2);

		elevator1.setState(Elevator.ElevatorStates.STUCKCLOSED);
		elevator2.setState(Elevator.ElevatorStates.STUCKOPEN);

		Thread e1 = new Thread(elevator1, "0");
		Thread e2 = new Thread(elevator2, "1");

		try {
			assertTrue(elevator1.getcurrentState().equals(Elevator.ElevatorStates.STUCKCLOSED));
			assertTrue(elevator2.getcurrentState().equals(Elevator.ElevatorStates.STUCKOPEN));

			e1.start();
			e2.start();

			Thread.sleep(2100);

			assertTrue(elevator1.getcurrentState().equals(Elevator.ElevatorStates.DOORSOPEN));
			assertTrue(elevator2.getcurrentState().equals(Elevator.ElevatorStates.DOORSCLOSED));

		} catch (InterruptedException e) {
			e.printStackTrace();
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
