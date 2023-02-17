/**
 * @Author: Mohamed Kaddour

 * @Date: 2023-02-16
 * @Version 2.0
 * 
 * As for Iteration1 and Version 1.0, Scheduler class acts as a buffer to transfer a message of type Message between both Floor and 
 * Elevator. The scheduler manages a queue that, for this iteration, will only hold up to 1 message. The message will then be passed
 * along to either the Floor class or the Elevator depending on the method call. 
 * 
 */
package source;

import java.util.ArrayList;
import java.util.HashMap;

public class Scheduler {
	
	public static final int REPLY_BUFFER_SIZE = 1;
	public static final int MESSAGE_BUFFER_FIRST_INDEX = 0;
	public static final int BUFFER_EMPTY = 0;
	
	public static final int ELEVATOR1 = 0;
	
	/*For now, these will have a maximum size of 1*/
	private ArrayList<Message> messageQueue;
	private ArrayList<Message> replyQueue;
	
	private boolean closed = false;
	private int messageRecieved;
	private int repliesRecieved;
	
	public static enum SchedulerStates {WAITING, RECEIVING, FAILURE, SENDING};
	
	private SchedulerStates states;
	
	private HashMap<Integer, ArrayList<Integer>> elevatorQueue;
	
	
	/**
	 * Constructor for class Scheduler. Initializes both the messageQueue and the replyQueue ArrayLists
	 */
	public Scheduler()
	{
		this.messageQueue = new ArrayList<>();
		this.replyQueue = new ArrayList<>();
		this.messageRecieved = 0;
		this.repliesRecieved = 0;
		this.elevatorQueue = new HashMap<>();
		this.elevatorQueue.put(ELEVATOR1, new ArrayList<Integer>());

		this.states = SchedulerStates.WAITING;
	}
	
	/**
	 * Synchronized method that takes in a message and, if the message queue is not full (size of 1), then it adds 
	 * the message to the queue. 
	 *
	 *@param message of type Message to pass
	 */
	public synchronized void passMessage(Message message)
	{

		
		while (states != SchedulerStates.WAITING) { //If the scheduler is not in the WAITING state, thread must wait until it is
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		} 
		this.states = SchedulerStates.RECEIVING; //Set to RECEIVING information
		
		this.messageQueue.add(message); //add the message to the message queue
		
		
		this.states = SchedulerStates.WAITING; //Set back to WAITING

		notifyAll();	//Let all threads know its ready
	}
	
	/**
	 * Synchronized method that returns a Message from the queue only if the queue is not empty, else it will wait. 
	 * @param none
	 * @return Message to be read
	 */
	public synchronized int readMessage()
	{

		while(states != SchedulerStates.WAITING) //If the scheduler is not in the WAITING state, thread must wait until it is
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		} 
		
		this.states = SchedulerStates.SENDING; //SENDING INFORMATION
		if(elevatorQueue.get(ELEVATOR1).size() == 0) { //IF THERE ARE NO NEW DESTINATIONS
			this.states = SchedulerStates.WAITING; //Back to WAITING
			return 0; 
		}
		int reply = this.elevatorQueue.get(ELEVATOR1).get(0); //Get the next destination and give it to the elevator
		this.elevatorQueue.get(ELEVATOR1).remove(0); //Remove the destination
		
		this.states = SchedulerStates.WAITING; // back to WAITING
		notifyAll();
		
		this.states = SchedulerStates.WAITING;
		
		return reply;
	}

	/**
	 * Synchronized method that takes in a message and, if the reply queue is not full (size of 1), then it adds 
	 * the message to the queue. 
	 *
	 *@param message of type Message to reply
	 */
	public synchronized void passState(int currentFloor, Elevator.ElevatorStates elevatorState, int elevatorNum)
	{
		while (states != SchedulerStates.WAITING) { //If the scheduler is not in the WAITING state, thread must wait until it is
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		states = SchedulerStates.RECEIVING; //Getting information
		if(messageQueue.size() != 0) { //Logic to get destinations for elevator, check if there are messages available
			int startFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).startFloor(); //Get start and destination floor of the request
			int destFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).destinationFloor();
			
			
			//Checks for logic
			boolean movingUp = elevatorState == Elevator.ElevatorStates.MOVINGUP;
			boolean movingDown = elevatorState == Elevator.ElevatorStates.MOVINGDOWN;
			boolean doorsClosed = elevatorState == Elevator.ElevatorStates.DOORSCLOSED;
			boolean startFloorBelow = startFloor < currentFloor;
			boolean startFloorAbove = startFloor > currentFloor;
			boolean startFloorEquals = startFloor == currentFloor;
			
			//Checks to see if the elevator is able to take on said request
			if ((movingUp && startFloorAbove) || (movingDown && startFloorBelow) || (startFloorEquals) || (doorsClosed && elevatorQueue.get(ELEVATOR1).isEmpty()))
			{
				this.elevatorQueue.get(ELEVATOR1).add(startFloor);
				this.elevatorQueue.get(ELEVATOR1).add(destFloor);
				this.messageQueue.remove(MESSAGE_BUFFER_FIRST_INDEX);
	
			}
		}
		
		//Add the return message of the elevator (current floor, state, and elevator number) for the floor to read
		Message reply = new Message("Elevator " + elevatorNum + ": is on floor " + currentFloor + " and is " + elevatorState);
		this.replyQueue.add(reply);
		states = SchedulerStates.WAITING; //Back to WAITING
		
		this.states = SchedulerStates.WAITING;
		
		notifyAll();	
	}
	
	/**
	 * Synchronized method that returns a Message from the queue only if the queue is not empty, else it will wait. 
	 * @param none
	 * @return Message to reply
	 * */
	public synchronized Message readReply()
	{
		while ((replyQueue.size() == 0) || (states != SchedulerStates.WAITING)) { //IF the replyQueue is empty or not in WAITING State
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		Message reply;
		
		states = SchedulerStates.SENDING; // SENDING INFO
		
		reply = this.replyQueue.get(BUFFER_EMPTY); //Get the request
		this.replyQueue.clear(); //empty Queue
		
		states = SchedulerStates.WAITING; //WAITING
		
		notifyAll();
		
		this.states = SchedulerStates.WAITING;
		
		return reply;
	}
	
	/**
	 * Getter to see if the scheduler is closed.
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
	
	/**
	 * Getter for the messages the scheduler has gotten from the floor
	 * @return messageRecieved, int
	 */
	public int getMessagesRecieved() {
		return this.messageRecieved;
	}
	
	/**
	 * Getter for the replies the scheduler has gotten from the elevator
	 * @return repliesRecieved, int
	 */
	public int getRepliesRecieved() {
		return this.repliesRecieved;
	}
	
	public Scheduler.SchedulerStates getSchedulerState()
	{
		return this.states;
	}
}
