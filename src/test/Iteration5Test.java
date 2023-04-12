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

	// Test Files
	private File badTestFile = new File("src//test//BadTestData.csv");
	private File badTestResortFile = new File("src//test//BadTestDataResort.csv");
	private File goodTestFile = new File("src//test//GoodTestData.csv");
	private File algorithmTestFile = new File("src//test//AlgorithmTest.csv");
	private File algorithmOppositeTestFile = new File("src//test//AlgorithmOppositeTestData.csv");

	// Port numbers 49152–65535 are ephemeral, and wont interfere with other
	// processes
	private static int elevatorPort = 51000;
	private static int floorHandlerPort = 50000;

	/**
	 * Run before each test
	 */
	@BeforeEach
	public void initialize() {
		incrementPorts();
	}

	/**
	 * Increment the ports to avoid errors with reusing same port numbers
	 */
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

		System.out.println("-=-=-=-=-= testElevatorErrorHandling =-=-=-=-=-=-");

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
					arrivedAtDestination = true;
					System.out.println("0");
				}
				if (ev.getcurrentState() == ElevatorStates.STUCKOPEN && !fixedDoors) {
					Thread.sleep(2050);
					if (ev.getcurrentState() == ElevatorStates.DOORSCLOSED) {
						fixedDoors = true;
						System.out.println("1");
					}
				}
				if (ev.getcurrentState() == ElevatorStates.TIMEOUT && !elevatorStuck) {
					System.out.println("2");
					elevatorStuck = true;
				}
				if (arrivedAtDestination && fixedDoors && elevatorStuck) {
					break;
				}
			}
			assertTrue(arrivedAtDestination);
			assertTrue(fixedDoors);
			assertTrue(elevatorStuck);
			
			e1.stop();
			eh1.stop();
			floorThread.stop();
			floorHandlerThread.stop();

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

		System.out.println("-=-=-=-=-= testFloorPorts =-=-=-=-=-=-");

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
			
			e1.stop();
			eh1.stop();
			floorThread.stop();
			floorHandlerThread.stop();
			reader.close();
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

		System.out.println("-=-=-=-=-= testElevatorPorts =-=-=-=-=-=-");

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
		
		e1.stop();
		eh1.stop();
		floorThread.stop();
		floorHandlerThread.stop();
	}

	/**
	 * From Iteration 2, Test validation method within Floor class, passing
	 * badTestFile, which contains a request in an improper format
	 * 
	 * @result test passes if the error message is displayed within the console
	 */
	@Test
	public void testIncorrectFormat() {

		System.out.println("-=-=-=-=-= testIncorrectFormat =-=-=-=-=-=-");

		Floor floor = new Floor(badTestFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		
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

		System.out.println("-=-=-=-=-= testElevatorStates =-=-=-=-=-=-");

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
				e1.stop();
				eh1.stop();
				floorThread.stop();
				floorHandlerThread.stop();
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

		System.out.println("-=-=-=-=-= testSchedulerStates =-=-=-=-=-=-");

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
				if(floor.getScanner() != null) {
					floor.closeScanner();					
				}
				e1.stop();
				eh1.stop();
				floorThread.stop();
				floorHandlerThread.stop();
				return; // will pass test if reaches return
			}
		}

	}

	/**
	 * Test to demonstrate an elevator receiving two requests, one with a start
	 * floor within the range of the other. The Scheduler should be capable of
	 * sorting the requests in the most efficient way, where it can make a stop to
	 * being another request while carrying out another request.
	 * 
	 * Test passes if the Scheduler sorts the requests such that the elevator will
	 * pick up the second request along the path of the first
	 */
	@Test
	public void testAlgorithmOneElevatorTwoRequests() {

		System.out.println("-=-=-=-=-= testAlgorithmOneElevatorTwoRequests =-=-=-=-=-=-");

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
			// handle only the first three requests from the file
			if (floor.getRequestsHandled() == 3) {
				floor.closeScanner();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (sch.getElevatorQueue().get(0).equals(expected)) {
				e1.stop();
				eh1.stop();
				floorThread.stop();
				floorHandlerThread.stop();
				return; // test passes
			}
		}
	}

	/**
	 * Tests that the scheduler will always provide highest priority in scheduling
	 * to an elevator with no requests
	 * 
	 * Test passes if each elevator receives a request
	 */
	@Test
	public void testAlgorithmDistribution() {

		System.out.println("-=-=-=-=-= testAlgorithmDistribution =-=-=-=-=-=-");

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
			// pass only 4 requests
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
				eh1.stop();
				eh2.stop();
				eh3.stop();
				eh4.stop();

				e1.stop();
				e2.stop();
				e3.stop();
				e4.stop();
				
				floorThread.stop();
				floorHandlerThread.stop();
				
				return; // test passes
			}
		}
	}

	/**
	 * Tests that the elevator is capable of finding the best elevator for a request
	 * when all elevators have a request currently being handled
	 * Tests the second priority for scheduling a new request
	 * 
	 * Passes if the elevator request goes to the best option for an elevator
	 */
	@Test
	public void testAlgorithmBestElevator() {

		System.out.println("-=-=-=-=-= testAlgorithmBestElevator =-=-=-=-=-=-");

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
			// allow only the first 5 requests
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
				eh1.stop();
				eh2.stop();
				eh3.stop();
				eh4.stop();

				e1.stop();
				e2.stop();
				e3.stop();
				e4.stop();
				
				floorThread.stop();
				floorHandlerThread.stop();
				
				return; // test passes
			}
		}
	}

	/**
	 * Tests how the elevator schedules a request that starts in the opposite
	 * direction that all the elevators are going
	 * 
	 * Test passes if the scheduler waits for one of the elevators to complete it's
	 * current request before assigning it to the newly freed elevator
	 */
	@Test
	public void testAlgorithmOppositeDirection() {

		System.out.println("-=-=-=-=-= testAlgorithmOppositeDirection =-=-=-=-=-=-");

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

		while (e1.isAlive()) {
			try {
				Thread.sleep(100);
				// allow only the first three requests
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
					eh1.stop();
					eh2.stop();

					e1.stop();
					e2.stop();
					
					floorThread.stop();
					floorHandlerThread.stop();
					
					return; // test passes
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * test if the Scheduler is capable of redistributing unstarted requests in the
	 * event that one of the Elevators encounters a hard fault
	 * 
	 * test passes if the requests are placed back in the Message queue for
	 * rescheduling
	 */
	@Test
	public void testAlgorithmRedistributeOnFailure() {

		System.out.println("-=-=-=-=-= testAlgorithmRedistributeOnFailure =-=-=-=-=-=-");

		Scheduler sch = new Scheduler(1);

		Elevator ev1 = new Elevator(elevatorPort, 1, Scheduler.TIMEOUT_DIASBLED);
		Thread e1 = new Thread(ev1, "0");
		Thread eh1 = new Thread(new ElevatorHandler(sch, elevatorPort, Scheduler.TIMEOUT_DIASBLED), "0");

		Floor floor = new Floor(badTestResortFile, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED);
		Thread floorThread = new Thread(floor, "Floor");
		Thread floorHandlerThread = new Thread(new FloorHandler(sch, floorHandlerPort, Scheduler.TIMEOUT_DIASBLED));

		eh1.start();
		e1.start();

		floorThread.start();
		floorHandlerThread.start();

		Message expectedMessage1 = new Message("17:38:44", 4, "UP", 6, 1);
		Message expectedMessage2 = new Message("17:38:45", 5, "UP", 7, 0);

		while (e1.isAlive()) {
			if (floor.getRequestsHandled() == 3 && sch.getMessages().contains(expectedMessage1)
					&& sch.getMessages().contains(expectedMessage2) && sch.getElevatorQueue().get(0).size() == 0) {
				eh1.stop();

				e1.stop();
				
				floorThread.stop();
				floorHandlerThread.stop();
				return; // test passes
			}
		}
	}
	
	/*aditional test cases 
	 * test each of the priorities for the elevator
	 * so far i tested highest priority as empty elevator and 
	 * scheduler finds closes elevator in distance 
	 * and scheduler does not schedule if elevator is moving in opposite direction
	 */

}
