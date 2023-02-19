package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import source.Elevator;
import source.Floor;
import source.Message;
import source.Scheduler;

public class Iteration2Test {

	private File testFile;
	private File badTestFile;

	@Before
	public void setUp() {
		this.testFile = new File("src//test//testData.csv");
	}

	@Test
	public void testSchedulerStates() {
		System.out.println("============ testsSchedulerStates ============");

		Scheduler sch = new Scheduler();
		Elevator ev = new Elevator(sch);

		System.out.println(sch.getSchedulerState());

		assertEquals(Scheduler.SchedulerStates.WAITING, sch.getSchedulerState());
		try {
			sch.passMessage(createRequest());
			Thread.sleep(500);

			System.out.println(sch.getPreviousSchedulerState());
			assertEquals(Scheduler.SchedulerStates.RECEIVING, sch.getPreviousSchedulerState());

			sch.passState(ev.getCurrentFloor(), ev.getcurrentState(), ev.getAssignedNum());
			Thread.sleep(500);

			System.out.println(sch.getPreviousSchedulerState());
			assertEquals(Scheduler.SchedulerStates.RECEIVING, sch.getPreviousSchedulerState());

			sch.readReply();
			Thread.sleep(500);

			System.out.println(sch.getPreviousSchedulerState());
			assertEquals(Scheduler.SchedulerStates.SENDING, sch.getPreviousSchedulerState());

			sch.readMessage();
			Thread.sleep(500);

			System.out.println(sch.getPreviousSchedulerState());
			assertEquals(Scheduler.SchedulerStates.SENDING, sch.getPreviousSchedulerState());

			Thread.sleep(500);

			System.out.println(sch.getSchedulerState());
			assertEquals(Scheduler.SchedulerStates.WAITING, sch.getSchedulerState());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testElevatorStates() {
		System.out.println("============ testElevatorStates ============");

		Scheduler sch = new Scheduler();
		Elevator ev = new Elevator(sch);

		Thread evThread = new Thread(ev, "Elevator");

		try {
			System.out.println(ev.getcurrentState());
			assertEquals(Elevator.ElevatorStates.DOORSCLOSED, ev.getcurrentState());

			sch.passMessage(createRequest());
			evThread.start();

			Thread.sleep(3500);
			System.out.println(ev.getcurrentState());
			assertEquals(Elevator.ElevatorStates.MOVINGUP, ev.getcurrentState());

			Thread.sleep(1000);
			assertEquals(2, ev.getCurrentFloor());

			System.out.println(ev.getcurrentState());
			assertEquals(Elevator.ElevatorStates.STOPPED, ev.getcurrentState());

			Thread.sleep(1000);
			System.out.println(ev.getcurrentState());
			assertEquals(Elevator.ElevatorStates.DOORSOPEN, ev.getcurrentState());

			Thread.sleep(1000);
			System.out.println(ev.getcurrentState());
			assertEquals(Elevator.ElevatorStates.DOORSCLOSED, ev.getcurrentState());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testPipeline() {
		System.out.println("============ TestPipeline ============");

		Scheduler sch = new Scheduler();
		Elevator ev = new Elevator(sch);
		Floor fl = new Floor(sch, testFile);

		Thread evThread = new Thread(ev, "Elevator");
		Thread flThread = new Thread(fl, "Floor");

		evThread.start();
		flThread.start();

		while (evThread.isAlive() || flThread.isAlive()) {
			// busy waiting
		}
		assertEquals(2, ev.getCurrentFloor());
	}

	@Test
	public void testIncorrectFormat() {
		badTestFile = new File("src//test//badTestData.csv");

		Scheduler sch = new Scheduler();
		Floor fl = new Floor(sch, badTestFile);

		Thread flThread = new Thread(fl, "Floor");

		flThread.start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Message createRequest() {

		Scanner reader;
		Message req = null;
		try {
			reader = new Scanner(testFile);
			String data = reader.nextLine();
			String[] values = data.split(", ");
			req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return req;
	}
}
