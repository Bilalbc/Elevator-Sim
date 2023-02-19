/**
 * @Author: Mohamed Kaddour

 * @Date: 2023-02-27
 * @Version 2.0
 * 
 * As for Iteration2 and Version 2.0, Scheduler class acts as a sorter for the floors based off the requests received from the floor subsystem. 
 * Once a message is received, it is added to the queue. It then adds the destination to the queue of a specific elevator thread. 
 * The scheduler also manages whether it would be efficient for a specific elevator (based on its number and current state) to take a certain 
 * request.
 */
package source;

import java.util.ArrayList;
import java.util.HashMap;

public class Scheduler {

	public static final int REPLY_BUFFER_SIZE = 1;
	public static final int MESSAGE_BUFFER_FIRST_INDEX = 0;
	public static final int BUFFER_EMPTY = 0;

	public static final int ELEVATOR1 = 0;

	/* For now, these will have a maximum size of 1 */
	private ArrayList<Message> messageQueue;
	private ArrayList<Message> replyQueue;

	private boolean closed = false;
	private boolean requestsComplete = false;

	public static enum SchedulerStates {
		WAITING, RECEIVING, FAILURE, SENDING
	};

	private SchedulerStates states;
	private SchedulerStates previousState;

	private HashMap<Integer, ArrayList<Integer>> elevatorQueue;

	/**
	 * Constructor for class Scheduler. Initializes messageQueue, replyQueue and the
	 * elevatorQueue for Elevator 1.
	 */
	public Scheduler() {
		this.messageQueue = new ArrayList<>();
		this.replyQueue = new ArrayList<>();
		this.elevatorQueue = new HashMap<>();
		this.elevatorQueue.put(ELEVATOR1, new ArrayList<Integer>());

		this.states = SchedulerStates.WAITING;
	}

	/**
	 * Synchronized method that takes in a message and, if the scheduler is not in a
	 * waiting state, adds the message to the queue. As of Iteration2, it is assumed
	 * there is no maximum capacity to the messageQueue.
	 *
	 * @param message of type Message to pass
	 */
	public synchronized void passMessage(Message message) {
		while (states != SchedulerStates.WAITING) { // If the scheduler is not in the WAITING state, thread must wait
													// until it is
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		this.states = SchedulerStates.RECEIVING; // Set to RECEIVING information

		this.messageQueue.add(message); // add the message to the message queue

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.states = SchedulerStates.WAITING; // Set to RECEIVING information
		this.previousState = SchedulerStates.RECEIVING;
		notifyAll(); // Let all threads know its ready
	}

	/**
	 * Synchronized method that returns a floor number to be the destination of the
	 * Elevator only in the case where that Elevator's queue is not empty. If the
	 * queue is not empty then return and then remove the first item in the floor
	 * queue.
	 * 
	 * @param none
	 * @return Floor to be read.
	 */
	public synchronized int readMessage() {

		while (states != SchedulerStates.WAITING) // If the scheduler is not in the WAITING state, thread must wait
													// until it is
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		if (elevatorQueue.get(ELEVATOR1).size() == 0) { // IF THERE ARE NO NEW DESTINATIONS
			return 0;
		}

		this.states = SchedulerStates.SENDING; // SENDING INFORMATIO

		int reply = this.elevatorQueue.get(ELEVATOR1).get(0); // Get the next destination and give it to the elevator
		this.elevatorQueue.get(ELEVATOR1).remove(0); // Remove the destination

		this.states = SchedulerStates.WAITING; // back to WAITING
		this.previousState = SchedulerStates.SENDING;
		notifyAll();

		return reply;
	}

	/**
	 * Synchronized method that takes in the state of the Elevator and depending on
	 * the current state and the current floor of the elevator, will determine
	 * whether the request's floor can be added to the elevator queue to maximize
	 * efficiency. As of Iteration2 there is only one elevator but elevatorNum will
	 * be used to manage multiple.
	 *
	 * Forms a reply to be sent to the Floor class.
	 * 
	 * @param currentFloor  int of the calling Elevator thread
	 * @param elevatorState the current state of the elevator of type
	 *                      Elevator.ElevatorStates
	 * @param elevatorNum,  the elevator number.
	 */
	public synchronized void passState(int currentFloor, Elevator.ElevatorStates elevatorState, int elevatorNum) {
		while (states != SchedulerStates.WAITING) { // If the scheduler is not in the WAITING state, thread must wait
													// until it is
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		states = SchedulerStates.RECEIVING; // Getting information
		this.previousState = SchedulerStates.RECEIVING;
		if (messageQueue.size() != 0) { // Logic to get destinations for elevator, check if there are messages available
			int startFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).startFloor(); // Get start and
																								// destination floor of
																								// the request
			int destFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).destinationFloor();

			// Checks for logic
			boolean movingUp = elevatorState == Elevator.ElevatorStates.MOVINGUP;
			boolean movingDown = elevatorState == Elevator.ElevatorStates.MOVINGDOWN;
			boolean doorsClosed = elevatorState == Elevator.ElevatorStates.DOORSCLOSED;
			boolean startFloorBelow = startFloor < currentFloor;
			boolean startFloorAbove = startFloor > currentFloor;
			boolean startFloorEquals = startFloor == currentFloor;

			// Checks to see if the elevator is able to take on said request
			if ((movingUp && startFloorAbove) || (movingDown && startFloorBelow) || (startFloorEquals)
					|| (doorsClosed && elevatorQueue.get(ELEVATOR1).isEmpty())) {
				this.elevatorQueue.get(ELEVATOR1).add(startFloor);
				this.elevatorQueue.get(ELEVATOR1).add(destFloor);
				this.messageQueue.remove(MESSAGE_BUFFER_FIRST_INDEX);

			}
		}
		// Add the return message of the elevator (current floor, state, and elevator
		// number) for the floor to read
		Message reply = new Message(
				"Elevator " + elevatorNum + ": is on floor " + currentFloor + " and is " + elevatorState);
		this.replyQueue.add(reply);

		this.states = SchedulerStates.WAITING;

		notifyAll();
	}

	/**
	 * Synchronized method that returns a Message from the queue only if the queue
	 * is not empty, else it will wait. The reply contains information on the
	 * current floor of the elevator.
	 * 
	 * @param none
	 * @return Message to reply
	 */
	public synchronized Message readReply() {
		// If the replyQueue is empty or not in WAITING State
		while ((replyQueue.size() == 0) || (states != SchedulerStates.WAITING)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		Message reply;

		states = SchedulerStates.SENDING; // SENDING INFO

		reply = this.replyQueue.get(BUFFER_EMPTY); // Get the request
		this.replyQueue.clear(); // empty Queue

		states = SchedulerStates.WAITING; // WAITING

		notifyAll();

		this.states = SchedulerStates.WAITING;
		this.previousState = SchedulerStates.SENDING;
		return reply;
	}

	/**
	 * Getter to see if the scheduler is closed.
	 * 
	 * @return closed, boolean
	 */

	public boolean isClosed() {
		return closed;
	}

	/**
	 * Sets the scheduler to closed
	 */
	public void setClosed() {
		this.closed = true;
	}

	public boolean isRequestsComplete() {
		return requestsComplete;
	}

	public void setRequestsComplete(boolean requestsComplete) {
		this.requestsComplete = requestsComplete;
	}

	public int getElevatorQueueSize() {
		return this.elevatorQueue.get(MESSAGE_BUFFER_FIRST_INDEX).size();
	}

	/**
	 * 
	 * Getter method that returns the current state of the Scheduler.
	 */
	public Scheduler.SchedulerStates getSchedulerState() {
		return this.states;
	}

	public Scheduler.SchedulerStates getPreviousSchedulerState() {
		return this.previousState;
	}
}
