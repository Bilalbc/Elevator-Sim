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
	private ArrayList<Integer> replyQueue;
	
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
		this.states = SchedulerStates.RECEIVING;

		/*
		while ((this.messageQueue.size() == MESSAGE_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		} */
		
		if (elevatorQueue.get(ELEVATOR1).isEmpty())
		{
			this.elevatorQueue.get(ELEVATOR1).add(message.startFloor());
			this.elevatorQueue.get(ELEVATOR1).add(message.destinationFloor());
		}
		else
		{
			this.messageQueue.add(message);
		}
		
		this.states = SchedulerStates.WAITING;

		//notifyAll();	
	}
	
	/**
	 * Synchronized method that returns a Message from the queue only if the queue is not empty, else it will wait. 
	 * @param none
	 * @return Message to be read
	 */
	public synchronized ArrayList<Integer> readMessage()
	{
		this.states = SchedulerStates.SENDING;
		
		/*
		while(this.messageQueue.size() != MESSAGE_BUFFER_SIZE)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		} */
		
		//notifyAll();
		
		this.states = SchedulerStates.WAITING;
		
		return this.elevatorQueue.get(ELEVATOR1);
	}

	/**
	 * Synchronized method that takes in a message and, if the reply queue is not full (size of 1), then it adds 
	 * the message to the queue. 
	 *
	 *@param message of type Message to reply
	 */
	public synchronized void passState(int currentFloor, Elevator.ElevatorStates elevatorState)
	{
		while ((this.replyQueue.size() == REPLY_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		int startFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).startFloor();
		int destFloor = this.messageQueue.get(MESSAGE_BUFFER_FIRST_INDEX).destinationFloor();
		
		boolean movingUp = elevatorState == Elevator.ElevatorStates.MOVINGUP;
		boolean movingDown = elevatorState == Elevator.ElevatorStates.MOVINGDOWN;
		boolean startFloorBelow = startFloor < currentFloor;
		boolean startFloorAbove = startFloor > currentFloor;
		boolean startFloorEquals = startFloor == currentFloor;
		
		if ((movingUp && startFloorAbove) || (movingDown && startFloorBelow) || (startFloorEquals))
		{
			this.elevatorQueue.get(ELEVATOR1).add(startFloor);
			this.elevatorQueue.get(ELEVATOR1).add(destFloor);
			this.messageQueue.remove(MESSAGE_BUFFER_FIRST_INDEX);
		}
		
		this.replyQueue.add(currentFloor);
		
		notifyAll();	
	}
	
	/**
	 * Synchronized method that returns a Message from the queue only if the queue is not empty, else it will wait. 
	 * @param none
	 * @return Message to reply
	 * */
	public synchronized Integer readReply()
	{
		Integer reply;
		
		while(this.replyQueue.size() != REPLY_BUFFER_SIZE)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		reply = this.replyQueue.get(BUFFER_EMPTY);
		this.replyQueue.clear();
		
		notifyAll();
		
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
}
