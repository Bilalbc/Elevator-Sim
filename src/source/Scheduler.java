/**
 * @Author: Mohamed Kaddour, Matthew Parker, Kousha Motazedian
 * @Date: 2023-03-23
 * @Version 4.0
 * 
 * As for Iteration4 and Version 4.0, Scheduler class acts as a sorter for the floors based off the requests received from the floor subsystem. 
 * Once a message is received, it is added to the queue. It then adds the destination to the queue of a specific elevator thread. 
 * The scheduler also manages whether it would be efficient for a specific elevator (based on its number and current state) to take a certain 
 * request.
 */

package source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import source.Elevator.ElevatorStates;

public class Scheduler {

	public static final int REPLY_BUFFER_SIZE = 1;
	public static final int MESSAGE_BUFFER_FIRST_INDEX = 0;
	public static final int REPLY_BUFFER_FIRST_INDEX = 0;
	public static final int ELEVATOR_QUEUE_FIRST_INDEX = 0;

	public static final int ELEVATOR1 = 0;
	public static final int ELEVATOR2 = 1;
	public static final int ELEVATOR3 = 2;
	public static final int ELEVATOR4 = 3;

	private ArrayList<Message> messageQueue;
	private ArrayList<Message> replyQueue;
	private ArrayList<Integer> elevatorFloors;
	private ArrayList<Elevator.ElevatorStates> elevatorStates;

	private boolean closed = false;
	private boolean requestsComplete = false;

	public static enum SchedulerStates {
		WAITING, RECEIVING, FAILURE, SENDING
	};

	private SchedulerStates states;
	private SchedulerStates previousState; // for testing purposes

	private HashMap<Integer, ArrayList<Integer>> elevatorQueue;

	/**
	 * Constructor for class Scheduler. Initializes messageQueue, replyQueue,
	 * elevatorFloors, elevatorStates and the elevatorQueue for all elevators.
	 */
	public Scheduler() {
		this.messageQueue = new ArrayList<>();
		this.replyQueue = new ArrayList<>();
		this.elevatorFloors = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
		this.elevatorStates = new ArrayList<>(
				Arrays.asList(Elevator.ElevatorStates.DOORSCLOSED, Elevator.ElevatorStates.DOORSCLOSED,
						Elevator.ElevatorStates.DOORSCLOSED, Elevator.ElevatorStates.DOORSCLOSED));
		this.elevatorQueue = new HashMap<>();
		
		this.elevatorQueue.put(ELEVATOR1, new ArrayList<Integer>());
		this.elevatorQueue.put(ELEVATOR2, new ArrayList<Integer>());
		this.elevatorQueue.put(ELEVATOR3, new ArrayList<Integer>());
		this.elevatorQueue.put(ELEVATOR4, new ArrayList<Integer>());

		this.states = SchedulerStates.WAITING;
	}

	/**
	 * Synchronized method that takes in a message and, if the scheduler is not in a
	 * waiting state, adds the message to the queue.
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

		// If the scheduler is not in the WAITING state, thread must wait until it is
		while (states != SchedulerStates.WAITING) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		int elevatorThreadNum = Integer.parseInt(Thread.currentThread().getName());

		if (elevatorQueue.get(elevatorThreadNum).size() == 0) { // IF THERE ARE NO NEW DESTINATIONS
			return 0;
		}

		this.states = SchedulerStates.SENDING; // SENDING INFORMATIO

		// Get the next destination and give it to the elevator
		int reply = this.elevatorQueue.get(elevatorThreadNum).get(ELEVATOR_QUEUE_FIRST_INDEX); 
		
		// if the elevator has arrived at its destination, remove the destination from the elevator's queue
		if(elevatorFloors.get(elevatorThreadNum) == elevatorQueue.get(elevatorThreadNum).get(ELEVATOR_QUEUE_FIRST_INDEX)) {
			elevatorQueue.get(elevatorThreadNum).remove(ELEVATOR_QUEUE_FIRST_INDEX);
		}

		this.states = SchedulerStates.WAITING; // back to WAITING
		this.previousState = SchedulerStates.SENDING;
		notifyAll();

		return reply;
	}

	/**
	 * Synchronized method that takes in the state of the Elevator and depending on
	 * the current state and the current floor of the elevator, will determine
	 * whether the request's floor can be added to the elevator queue to maximize
	 * efficiency.
	 *
	 * Forms a reply to be sent to the Floor class.
	 * 
	 * @param PassStateEvent pse encapsulating all information. 
	 */
	public synchronized void passState(PassStateEvent pse) {
		int currentFloor = pse.getCurrentFloor();
		Elevator.ElevatorStates elevatorState = pse.getCurrentState();
		int elevatorNum = pse.getAssignedNum();

		while (states != SchedulerStates.WAITING) { // If the scheduler is not in the WAITING state, thread must wait
			// until it is
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		if (elevatorState == Elevator.ElevatorStates.TIMEOUT)
		{
			System.out.println("Elevator " + elevatorNum + " has timed out before reaching next floor...elevator shutting down");
			this.elevatorQueue.get(elevatorNum).clear();
		}

		// Update current floor and current state of called elevator
		elevatorFloors.set(elevatorNum - 1, currentFloor);
		elevatorStates.set(elevatorNum - 1, elevatorState);

		states = SchedulerStates.RECEIVING; // Getting information
		this.previousState = SchedulerStates.RECEIVING;

		schedulerAlgorithm();

		// Add the return message of the elevator (current floor, state, and elevator
		// number) for the floor to read
		Message reply = new Message(
				"Elevator " + elevatorNum + ": is on floor " + currentFloor + " and is " + elevatorState);

		this.replyQueue.add(reply);
		this.states = SchedulerStates.WAITING;

		if (checkFinished()) {
			System.exit(0);
		}

		notifyAll();
	}

	/**
	 * Method containing the algorithm to efficiently distribute incoming requests
	 * among the active elevators
	 */
	public void schedulerAlgorithm() {
		ArrayList<Integer> validElevators = new ArrayList<>();
		boolean valid = false;

		int startFloor = 0;
		int destFloor = 0;

		if (messageQueue.size() != 0) { // Logic to get destinations for elevator, check if there are messages available
			// Get start and destination floor of the request

			startFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).startFloor();
			destFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).destinationFloor();
			String requestDirection = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).getDirection();
			for (int i : elevatorQueue.keySet()) {

				// Checks for logic
				boolean movingUp = elevatorStates.get(i) == Elevator.ElevatorStates.MOVINGUP;
				boolean movingDown = elevatorStates.get(i) == Elevator.ElevatorStates.MOVINGDOWN;
				boolean doorsClosed = elevatorStates.get(i) == Elevator.ElevatorStates.DOORSCLOSED;
				boolean requestUp = movingUp && (requestDirection.equals("UP"));
				boolean requestDown = movingDown && (requestDirection.equals("DOWN"));
				boolean startFloorBelow = startFloor < elevatorFloors.get(i);
				boolean startFloorAbove = startFloor > elevatorFloors.get(i);
				boolean startFloorEquals = startFloor == elevatorFloors.get(i);
				boolean up = (startFloor - destFloor) < 0;

				// Checks to see if the elevator is able to take on said request
				if ((requestUp && startFloorAbove) || (requestDown && startFloorBelow) || (startFloorEquals && ((up && movingUp) || (!up && movingDown) ))
						|| (doorsClosed && elevatorQueue.get(i).isEmpty())) {
					
					
					validElevators.add(i);
					valid = true;	
				}
			}
		}

		// If an elevator(s) is found to be able to take on request, checks for shortest
		// distance from start floor
		if (valid == true) {
			int closestElevator = validElevators.get(0);
			int shortestDistance = Math.abs(elevatorFloors.get(validElevators.get(0)) - startFloor);
			for (int i = 0; i < validElevators.size(); i++) {
				int floorDistance = Math.abs(elevatorFloors.get(validElevators.get(i)) - startFloor);
				if (elevatorQueue.get(validElevators.get(i)).isEmpty()) {
					closestElevator = validElevators.get(i);
					break;
				} else if (shortestDistance > floorDistance) {
					shortestDistance = floorDistance;
					closestElevator = validElevators.get(i);
				}
			}
			
			int size = elevatorQueue.get(closestElevator).size();
			if(size != 1) {
				this.elevatorQueue.get(closestElevator).add(0, destFloor);
				this.elevatorQueue.get(closestElevator).add(0, startFloor);
			}
			else {
				this.elevatorQueue.get(closestElevator).add(startFloor);
				this.elevatorQueue.get(closestElevator).add(destFloor);
			}
			
			size = elevatorQueue.get(closestElevator).size();
			this.messageQueue.remove(MESSAGE_BUFFER_FIRST_INDEX);
			
			// if elevator is moving down, sort in descending order
			if(elevatorStates.get(closestElevator) == ElevatorStates.MOVINGDOWN) {
				if((size > 2) && (elevatorQueue.get(closestElevator).get(size - 2) - elevatorQueue.get(closestElevator).get(size - 1) < 0)) {
					Collections.reverse(elevatorQueue.get(closestElevator).subList(0, size - 3));
				}
				else if((size > 2) && (elevatorQueue.get(closestElevator).get(0) - elevatorQueue.get(closestElevator).get(1) < 0)) {
					Collections.reverse(elevatorQueue.get(closestElevator).subList(2, size - 1));
				}
				else {
					this.elevatorQueue.get(closestElevator).sort(Collections.reverseOrder());
				}
			} 
			// if elevator is moving up, sort in ascending order 
			else if (elevatorStates.get(closestElevator) == ElevatorStates.MOVINGUP) {
				
				if((size > 2) && (elevatorQueue.get(closestElevator).get(size - 2) - elevatorQueue.get(closestElevator).get(size - 1) < 0)) {
					Collections.reverse(elevatorQueue.get(closestElevator).subList(0, size - 3));
				}
				else if((size > 2) && (elevatorQueue.get(closestElevator).get(0) - elevatorQueue.get(closestElevator).get(1) < 0)) {
					Collections.reverse(elevatorQueue.get(closestElevator).subList(2, size - 1));
				}
				else {
					Collections.sort(elevatorQueue.get(closestElevator));
				}
			}
		}
	}

	/**
	 * Helper method to determine whether the program is complete.
	 * 
	 * checks if: All the elevators are DOORSCLOSED All the elevator requests have
	 * been handled The Floor has notified the requests are complete
	 * 
	 * @return boolean : Whether the program is complete or not
	 */
	private boolean checkFinished() {
		boolean elevatorsComplete = true;
		boolean elevatorStatesClosed = true;

		for (Elevator.ElevatorStates i : elevatorStates) {
			if (!(i == Elevator.ElevatorStates.DOORSCLOSED)) {
				elevatorStatesClosed = false;
			}
		}

		for (ArrayList<Integer> i : elevatorQueue.values()) {
			if (i.size() > 0) {
				elevatorsComplete = false;
			}
		}

		if (this.requestsComplete && elevatorStatesClosed && elevatorsComplete) {
			return true;
		}
		return false;

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

		reply = this.replyQueue.get(REPLY_BUFFER_FIRST_INDEX); // Get the request
		this.replyQueue.remove(REPLY_BUFFER_FIRST_INDEX); // empty Queue

		states = SchedulerStates.WAITING; // WAITING

		notifyAll();

		this.previousState = SchedulerStates.SENDING;
		return reply;
	}

	/**
	 * Getter to see if the scheduler is closed.
	 * 
	 * @return boolean: closed
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

	/**
	 * Getter method to check the boolean flag if requests are complete
	 * 
	 * @return boolean : if requests are complete
	 */
	public boolean isRequestsComplete() {
		return requestsComplete;
	}

	/**
	 * Setter method to set the boolean flag requestsComplete to provided value,
	 * Used by Floor to signal no more requests are to be passed
	 * 
	 * @param requestsComplete : indicates if more requests are to come
	 */
	public void setRequestsComplete(boolean requestsComplete) {
		this.requestsComplete = requestsComplete;
	}

	/**
	 * Getter method that returns the current size of the elevator queue
	 * 
	 * @return int : size of the elevator queue
	 */
	public int getElevatorQueueSize() {
		return this.elevatorQueue.get(MESSAGE_BUFFER_FIRST_INDEX).size();
	}

	/**
	 * Getter method that returns the current state of the Scheduler.
	 * 
	 * @return SchedulerStates : current state of the Scheduler
	 */
	public SchedulerStates getSchedulerState() {
		return this.states;
	}

	/**
	 * Getter method that returns the previous state of the Scheduler
	 * 
	 * @return SchedulerStates : previous state of the Scheduler
	 */
	public SchedulerStates getPreviousSchedulerState() {
		return this.previousState;
	}

	/**
	 * Getter method for the messageQueue ArrayList for testing purposes
	 * 
	 * @return ArrayList<Message> : currently stored messages
	 */
	public ArrayList<Message> getMessages() {
		return this.messageQueue;
	}

	/**
	 * Getter method for the replyQueue ArrayList for testing purposes
	 * 
	 * @return ArrayList<Message> : currently stored replies
	 */
	public ArrayList<Message> getReplies() {
		return this.replyQueue;
	}

	/**
	 * Getter method for the elevatorQueue Map for testing purposes
	 * 
	 * @return HashMap<Integer, ArrayList<Integer>> : Elevators and prepared Queues
	 */
	public HashMap<Integer, ArrayList<Integer>> getElevatorQueue() {
		return this.elevatorQueue;
	}

	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		Thread fh = new Thread(new FloorHandler(s));

		Thread eh1 = new Thread(new ElevatorHandler(s, 69), "0");
		Thread eh2 = new Thread(new ElevatorHandler(s, 70), "1");
		Thread eh3 = new Thread(new ElevatorHandler(s, 71), "2");
		Thread eh4 = new Thread(new ElevatorHandler(s, 72), "3");

		fh.start();
		eh1.start();
		eh2.start();
		eh3.start();
		eh4.start();

	}
}