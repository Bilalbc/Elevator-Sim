package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
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

	private Scheduler sch;
	private Elevator ev1;
	private Elevator ev2;
	private Elevator ev3;
	private Elevator ev4;

	private Thread e1;
	private Thread e2;
	private Thread e3;
	private Thread e4;

	private ElevatorHandler eh1;
	private ElevatorHandler eh2;
	private ElevatorHandler eh3;
	private ElevatorHandler eh4;

	private Thread eht1;
	private Thread eht2;
	private Thread eht3;
	private Thread eht4;

	private FloorHandler floorHandler;
	private Thread fht;

	/**
	 * Execute before test cases Setup testFile containing one entry of a proper
	 * request, and a badTestFile containing one entry of an improper request.
	 */

	@AfterEach
	public void reset() {
		e1.suspend();
		e2.suspend();
		e3.suspend();
		e4.suspend();

		eht1.suspend();
		eht2.suspend();
		eht3.suspend();
		eht4.suspend();

		fht.suspend();

		ev1.closeSockets();
		ev2.closeSockets();
		ev3.closeSockets();
		ev4.closeSockets();

		eh1.closeSockets();
		eh2.closeSockets();
		eh3.closeSockets();
		eh4.closeSockets();

		floorHandler.closeSockets();

		sch.setClosed();
	}

	private void init(Floor floor) {
		sch = new Scheduler();

		ev1 = new Elevator(69, 1);
		ev2 = new Elevator(70, 2);
		ev3 = new Elevator(71, 3);
		ev4 = new Elevator(72, 4);

		e1 = new Thread(ev1, "0");
		e2 = new Thread(ev2, "1");
		e3 = new Thread(ev3, "2");
		e4 = new Thread(ev4, "3");

		eh1 = new ElevatorHandler(sch, 69);
		eh2 = new ElevatorHandler(sch, 70);
		eh3 = new ElevatorHandler(sch, 71);
		eh4 = new ElevatorHandler(sch, 72);

		eht1 = new Thread(eh1, "0");
		eht2 = new Thread(eh2, "1");
		eht3 = new Thread(eh3, "2");
		eht4 = new Thread(eh4, "3");

		floorHandler = new FloorHandler(sch);
		fht = new Thread(floorHandler);
	}

	/**
	 * Test case for Elevator error states from Iteration 4.
	 * 
	 * @result test passes if elevator can successfully recover from failure, and
	 *         enters TIMEOUT state on hard error
	 */
	@Test
	public void testElevatorTimeoutHandling() {
		Floor floor = new Floor(badTestFile);
		Thread floorThread = new Thread(floor, "Floor");
		init(floor);
		floorThread.start();
		fht.start();

		e1.start();
		e2.start();
		e3.start();
		e4.start();

		eht1.start();
		eht2.start();
		eht3.start();
		eht4.start();

		try {
			boolean arrivedAtDestination = false;
			boolean fixedDoors = false;
			boolean elevatorStuck = false;

			while (e1.isAlive() || e2.isAlive() || e3.isAlive()) {
				if (ev1.getCurrentFloor() == ev1.getDestinationFloor()) {
					arrivedAtDestination = true;
				}
				if (ev2.getcurrentState() == ElevatorStates.STUCKOPEN) {
					Thread.sleep(2050);
					if (ev2.getcurrentState() == ElevatorStates.DOORSCLOSED) {
						fixedDoors = true;
					}
				}
				if (ev3.getcurrentState() == ElevatorStates.TIMEOUT) {
					elevatorStuck = true;
				}
				if (arrivedAtDestination && fixedDoors && elevatorStuck) {
					break;
				}
			}
			assertTrue(arrivedAtDestination);
			assertTrue(fixedDoors);
			assertTrue(elevatorStuck);
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
			Floor floor = new Floor(goodTestFile);
			Thread floorThread = new Thread(floor, "Floor");

			init(floor);

			floorThread.start();
			fht.start();

			e1.start();
			eht1.start();

			Floor utilFloor = new Floor();

			Scanner reader = new Scanner(goodTestFile);
			Message m = utilFloor.createRequest(reader);
			Thread.sleep(1000);
			assertTrue(m.equals(sch.getElevatorRequests().get(0).get(0)));
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

		Floor floor = new Floor(goodTestFile);
		Thread floorThread = new Thread(floor, "Floor");

		init(floor);
		floorThread.start();
		fht.start();

		e1.start();
		eht1.start();

		Message m = new Message("Elevator 1: is on floor 1 and is DOORSCLOSED");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(floor.getLatestReturned());
		assertTrue(m.getReturnMessage().equals(floor.getLatestReturned().getReturnMessage()));
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
		Floor floor = new Floor(badTestFile);

		init(floor);

		Scanner reader = new Scanner("18as:21:dd, 1, UP, 3, 0");
		floor.createRequest(reader);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		Floor floor = new Floor(goodTestFile);
		Thread floorThread = new Thread(floor, "Floor");

		init(floor);
		floorThread.start();
		fht.start();

		e1.start();
		eht1.start();

		boolean doorsClosed = false;
		boolean movingUp = false;
		boolean stopped = false;
		boolean doorsOpen = false;

		while (true) {
			if (ev1.getcurrentState() == ElevatorStates.DOORSCLOSED && !doorsClosed) {
				System.out.println("DOORSCLOSED Detected");
				doorsClosed = true;
			} else if (ev1.getcurrentState() == ElevatorStates.MOVINGUP && !movingUp) {
				System.out.println("MOVINGUP Detected");
				movingUp = true;
			} else if (ev1.getcurrentState() == ElevatorStates.STOPPED && !stopped) {
				System.out.println("STOPPED Detected");
				stopped = true;
			} else if (ev1.getcurrentState() == ElevatorStates.DOORSOPEN && !doorsOpen) {
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

		Floor floor = new Floor(goodTestFile);
		init(floor);

		Thread floorThread = new Thread(floor, "Floor");
		floorThread.start();
		fht.start();

		e1.start();
		eht1.start();

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

		Elevator ev = new Elevator(69, 1);
		Thread e1 = new Thread(ev, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, 69), "0");

		Floor floor = new Floor(algorithmTestFile);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch));
		
		e1.start();
		eh1.start();
		floorThread.start();
		floorHandlerThread.start();
		
		while(e1.isAlive()) {
			// handle only the first two requests from the file
			if(floor.getRequestsHandled() == 2) {
				floor.closeScanner();
			}
			System.out.println(sch.getElevatorQueue());
			System.out.println(ev.getCurrentFloor());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}

}
