package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import source.Elevator;
import source.Elevator.ElevatorStates;
import source.ElevatorHandler;
import source.Floor;
import source.FloorHandler;
import source.Message;
import source.Scheduler;
import source.Scheduler.SchedulerStates;

public class Iteration5Test {

	private File badTestFile = new File("src//test//BadTestData.csv");
	private File goodTestFile = new File("src//test//GoodTestData.csv");
	private File algorithmTestFile = new File("src//test//AlgorithmTest.csv");

	private File algorithmOppositeTestFile = new File("src//test//AlgorithmOppositeTestData.csv");
	private static int elevatorPort = 51000;
	private static int floorHandlerPort = 50000;

	@BeforeEach
	public void initialize() {
		incrementPorts();
	}

	private void incrementPorts() {
		elevatorPort++;
		floorHandlerPort++;
	}

	/**
	 * Test case for Elevator error states from Iteration 4.
	 * 
	 * @result test passes if elevator can successfully recover from failure, and
	 *         enters TIMEOUT state on hard error
	 */
	@Test
	public void testElevatorErrorHandling() {
		System.out.println(elevatorPort);
		System.out.println(floorHandlerPort);
		Scheduler sch = new Scheduler(1);

		Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev, "0");

		ElevatorHandler elevatorHandler = new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED);
		Thread eh1 = new Thread(elevatorHandler, "0");

		Floor floor = new Floor(badTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		FloorHandler floorHandler = new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorHandlerThread = new Thread(floorHandler);

		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();

		try {
			boolean arrivedAtDestination = false;
			boolean fixedDoors = false;
			boolean elevatorStuck = false;

			while (e1.isAlive()) {
				if (ev.getCurrentFloor() == ev.getDestinationFloor() && !arrivedAtDestination) {
					System.out.println("0");
					arrivedAtDestination = true;
				}
				if (ev.getcurrentState() == ElevatorStates.STUCKOPEN && !fixedDoors) {
					System.out.println("1");
					Thread.sleep(2050);
					if (ev.getcurrentState() == ElevatorStates.DOORSCLOSED) {
						fixedDoors = true;
					}
				}
				if (ev.getcurrentState() == ElevatorStates.TIMEOUT && !elevatorStuck) {
					System.out.println("2");
					elevatorStuck = true;
				}
				if (arrivedAtDestination && fixedDoors && elevatorStuck) {
					System.out.println("Hello");
					break;
				}
			}
			assertTrue(arrivedAtDestination);
			assertTrue(fixedDoors);
			assertTrue(elevatorStuck);

			floor.closeScanner();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test case from Iteration 3 for ports between Floor and FloorHandler. tests by
	 * sending a simulated message from the floor class to the handler, and then
	 * confirms the message that is saved in the scheduler matches.
	 * 
	 * @result test passes if the message in the scheduler matches the message in
	 *         the testFile
	 */
	@Test
	public void testFloorPorts() {
		System.out.println("---------------");
		try {
			Scheduler sch = new Scheduler(1);

			Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
			Thread e1 = new Thread(ev, "0");
			Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

			Floor floor = new Floor(badTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
			Thread floorThread = new Thread(floor, "Floor");
			Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

			e1.start();
			eh1.start();
			floorThread.start();
			floorHandlerThread.start();

			Floor utilFloor = new Floor();

			Scanner reader = new Scanner(goodTestFile);
			Message m = utilFloor.createRequest(reader);
			Thread.sleep(2000);
			assertTrue(m.equals(sch.getElevatorRequests().get(0).get(0)));

			floor.closeScanner();
		} catch (FileNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test case for ports from Iteration 3 between Elevator and ElevatorHandler,
	 * confirms if the reply sent from an elevator is the same as what is stored in
	 * the scheduler as a reply
	 * 
	 * @result Test passes if the contents of the reply in Scheduler is the same as
	 *         what was sent from elevator
	 */

	@Test
	public void testElevatorPorts() {
		System.out.println("---------------");

		Scheduler sch = new Scheduler(1);

		Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		Floor floor = new Floor(goodTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();

		Message m = new Message("Elevator 1: is on floor 1 and is DOORSCLOSED");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(floor.getLatestReturned());
		assertTrue(m.getReturnMessage().equals(floor.getLatestReturned().getReturnMessage()));

		floor.closeScanner();
	}

	/**
	 * From Iteration 2, Test validation method within Floor class, passing
	 * badTestFile, which contains a request in an improper format
	 * 
	 * @result test passes if the error message is displayed within the console
	 */
	@Test
	public void testIncorrectFormat() {
		System.out.println("============ testIncorrectFormat ============");

		System.out.println(elevatorPort);
		System.out.println(floorHandlerPort);
		Scheduler sch = new Scheduler(1);

		Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		Floor floor = new Floor(badTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();

		Scanner reader = new Scanner("18as:21:dd, 1, UP, 3, 0");
		floor.createRequest(reader);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		floor.closeScanner();
	}

	/**
	 * From Iteration 2, Test states and events for Elevator class, asserting the
	 * expected state after an event is made. Events simulate the standard execution
	 * expected
	 * 
	 * Tests are not concerned with the contents of the messages being passed, nor
	 * the output of the method, just the state transitions.
	 * 
	 * @result test passes if the Elevator enters all the correct states after each
	 *         event
	 */
	@Test
	public void testElevatorStates() {
		Scheduler sch = new Scheduler(1);

		System.out.println(elevatorPort);
		System.out.println(floorHandlerPort);
		Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		Floor floor = new Floor(goodTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();

		boolean doorsClosed = false;
		boolean movingUp = false;
		boolean stopped = false;
		boolean doorsOpen = false;

		while (true) {
			if (ev.getcurrentState() == ElevatorStates.DOORSCLOSED && !doorsClosed) {
				System.out.println("DOORSCLOSED Detected");
				doorsClosed = true;
			} else if (ev.getcurrentState() == ElevatorStates.MOVINGUP && !movingUp) {
				System.out.println("MOVINGUP Detected");
				movingUp = true;
			} else if (ev.getcurrentState() == ElevatorStates.STOPPED && !stopped) {
				System.out.println("STOPPED Detected");
				stopped = true;
			} else if (ev.getcurrentState() == ElevatorStates.DOORSOPEN && !doorsOpen) {
				System.out.println("DOORSOPEND Detected");
				doorsOpen = true;
			}

			if (doorsClosed && movingUp && stopped && doorsOpen) {
				floor.closeScanner();
				return; // will pass test if reaches return
			}
		}
	}

	/**
	 * From Iteration 2, Test states and events for scheduler class, asserting the
	 * expected state after an event is made. Events simulate the standard execution
	 * expected
	 * 
	 * Tests are not concerned with the contents of the messages being passed, nor
	 * the output of the method, just the state transitions
	 * 
	 * @result test passes if the Scheduler enters all the correct states after each
	 *         event
	 */
	@Test
	public void testSchedulerStates() {
		System.out.println("============ testsSchedulerStates ============");

		Scheduler sch = new Scheduler(1);

		Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		Floor floor = new Floor(goodTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();

		boolean waiting = false;
		boolean receiving = false;
		boolean sending = false;

		while (true) {
			if (sch.getCurrentState() == SchedulerStates.WAITING && !waiting) {
				System.out.println("WAITING Detected");
				waiting = true;
			} else if (sch.getCurrentState() == SchedulerStates.RECEIVING && !receiving) {
				System.out.println("RECEIVING Detected");
				receiving = true;
			} else if (sch.getCurrentState() == SchedulerStates.SENDING && !sending) {
				System.out.println("SENDING Detected");
				sending = true;
			}

			if (waiting && receiving && sending) {
				floor.closeScanner();
				return; // will pass test if reaches return
			}
		}

	}

	@Test
	public void testAlgorithmOneElevatorTwoRequests() {
		Scheduler sch = new Scheduler(1);

		Elevator ev = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		Floor floor = new Floor(algorithmTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();

		ArrayList<Integer> expected = new ArrayList<>(Arrays.asList(5, 5, 7, 9));
		while (e1.isAlive()) {
			// handle only the first two requests from the file
			if (floor.getRequestsHandled() == 3) {
				floor.closeScanner();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (sch.getElevatorQueue().get(0).equals(expected)) {
				floor.closeScanner();
				return;
			}
		}
	}

	@Test
	public void testAlgorithmDistribution() {
		Scheduler sch = new Scheduler(4);

		Elevator ev1 = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev1, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		incrementPorts();

		Elevator ev2 = new Elevator(elevatorPort, 2, Scheduler.TIMEOUT_DIASBLED);
		Thread e2 = new Thread(ev2, "1");
		Thread eh2 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "1");

		incrementPorts();

		Elevator ev3 = new Elevator(elevatorPort, 3, Scheduler.TIMEOUT_DIASBLED);
		Thread e3 = new Thread(ev3, "2");
		Thread eh3 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "2");

		incrementPorts();

		Elevator ev4 = new Elevator(elevatorPort, 4, Scheduler.TIMEOUT_DIASBLED);
		Thread e4 = new Thread(ev4, "3");
		Thread eh4 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "3");

		Floor floor = new Floor(algorithmTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		eh1.start();
		eh2.start();
		eh3.start();
		eh4.start();

		e1.start();
		e2.start();
		e3.start();
		e4.start();

		floorThread.start();
		floorHandlerThread.start();

		ArrayList<Integer> expectedEV1 = new ArrayList<>(Arrays.asList(2, 5));
		ArrayList<Integer> expectedEV2 = new ArrayList<>(Arrays.asList(3, 7));
		ArrayList<Integer> expectedEV3 = new ArrayList<>(Arrays.asList(5, 9));
		ArrayList<Integer> expectedEV4 = new ArrayList<>(Arrays.asList(7, 11));

		boolean ev1Pass = false;
		boolean ev2Pass = false;
		boolean ev3Pass = false;
		boolean ev4Pass = false;

		while (e1.isAlive()) {
			if (floor.getRequestsHandled() == 4) {
				floor.closeScanner();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (sch.getElevatorQueue().get(0).equals(expectedEV1) && !ev1Pass) {
				ev1Pass = true;
			}
			if (sch.getElevatorQueue().get(1).equals(expectedEV2) && !ev2Pass) {
				ev2Pass = true;
			}
			if (sch.getElevatorQueue().get(2).equals(expectedEV3) && !ev3Pass) {
				ev3Pass = true;
			}
			if (sch.getElevatorQueue().get(3).equals(expectedEV4) && !ev4Pass) {
				ev4Pass = true;
			}

			if (ev1Pass && ev2Pass && ev3Pass & ev4Pass) {
				floor.closeScanner();
				return;
			}
		}
	}

	@Test
	public void testAlgorithmBestElevator() {
		Scheduler sch = new Scheduler(4);

		Elevator ev1 = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev1, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		incrementPorts();

		Elevator ev2 = new Elevator(elevatorPort, 2, Scheduler.TIMEOUT_DIASBLED);
		Thread e2 = new Thread(ev2, "1");
		Thread eh2 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "1");

		incrementPorts();

		Elevator ev3 = new Elevator(elevatorPort, 3, Scheduler.TIMEOUT_DIASBLED);
		Thread e3 = new Thread(ev3, "2");
		Thread eh3 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "2");

		incrementPorts();

		Elevator ev4 = new Elevator(elevatorPort, 4, Scheduler.TIMEOUT_DIASBLED);
		Thread e4 = new Thread(ev4, "3");
		Thread eh4 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "3");

		Floor floor = new Floor(algorithmTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		eh1.start();
		eh2.start();
		eh3.start();
		eh4.start();

		e1.start();
		e2.start();
		e3.start();
		e4.start();

		floorThread.start();
		floorHandlerThread.start();

		ArrayList<Integer> expectedEV1 = new ArrayList<>(Arrays.asList(5, 6, 7));
		ArrayList<Integer> expectedEV2 = new ArrayList<>(Arrays.asList(3, 7));
		ArrayList<Integer> expectedEV3 = new ArrayList<>(Arrays.asList(5, 9));
		ArrayList<Integer> expectedEV4 = new ArrayList<>(Arrays.asList(7, 11));

		boolean ev1Pass = false;
		boolean ev2Pass = false;
		boolean ev3Pass = false;
		boolean ev4Pass = false;

		while (e1.isAlive()) {
			if (floor.getRequestsHandled() == 5) {
				floor.closeScanner();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (sch.getElevatorQueue().get(0).equals(expectedEV1) && !ev1Pass) {
				ev1Pass = true;
			}
			if (sch.getElevatorQueue().get(1).equals(expectedEV2) && !ev2Pass) {
				ev2Pass = true;
			}
			if (sch.getElevatorQueue().get(2).equals(expectedEV3) && !ev3Pass) {
				ev3Pass = true;
			}
			if (sch.getElevatorQueue().get(3).equals(expectedEV4) && !ev4Pass) {
				ev4Pass = true;
			}

			if (ev1Pass && ev2Pass && ev3Pass & ev4Pass) {
				floor.closeScanner();
				return;
			}
		}
	}

	@Test
	public void testAlgorithmOppositeDirection() {
		Scheduler sch = new Scheduler(2);

		Elevator ev1 = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev1, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		incrementPorts();

		Elevator ev2 = new Elevator(elevatorPort, 2, Scheduler.TIMEOUT_DIASBLED);
		Thread e2 = new Thread(ev2, "1");
		Thread eh2 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "1");

		Floor floor = new Floor(algorithmOppositeTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		eh1.start();
		eh2.start();
		e1.start();
		e2.start();

		floorThread.start();
		floorHandlerThread.start();

		ArrayList<Integer> expectedEV1 = new ArrayList<>(Arrays.asList(3));
		ArrayList<Integer> expectedEV2 = new ArrayList<>(Arrays.asList(3, 7));
		ArrayList<Integer> expectedOppositeDirection = new ArrayList<>(Arrays.asList(2, 1));
		
		boolean ev1Pass = false;
		boolean ev2Pass = false;
		boolean oppositeDirectionPass = false;
		
		while(e1.isAlive()) {
			try {
				System.out.println(sch.getElevatorQueue());
				Thread.sleep(100);
				if (floor.getRequestsHandled() == 3) {
					floor.closeScanner();
				}	
				
				if (sch.getElevatorQueue().get(0).equals(expectedEV1) && !ev1Pass) {
					ev1Pass = true;
				}
				if (sch.getElevatorQueue().get(1).equals(expectedEV2) && !ev2Pass) {
					ev2Pass = true;
				}
				if (sch.getElevatorQueue().get(0).equals(expectedOppositeDirection) && !oppositeDirectionPass) {
					oppositeDirectionPass = true;
				}
				if (ev1Pass && ev2Pass && oppositeDirectionPass) {
					floor.closeScanner();
					return;
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
