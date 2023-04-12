package source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Akshay V., Kousha Motazedian, Matthew Parker
 * @Date: 2023-03-25
 * @Version 4.0
 *
 *          An Elevator class that receives floor requests from the scheduler
 *          which indicates the order in which the elevator should pick up/drop
 *          off riders. As of Iteration2, the elevator follows the states of
 *          being able to move up, move down, stop, close doors and open doors.
 */

public class Elevator implements Runnable {

	private int currentFloor;
	private int assignedNum;
	private int destination = 0; // current destination floor, 0 means no destination at the moment
	private int handlerPort;
	private int errorCode;
	private DatagramSocket sendAndReceive;
	private boolean running; 
	
	private static final int TIME_TO_MOVE = 4083;
	private static final int TIME_DOORS = 1000;
	private static final int MAX_DATA_SIZE = 250;	
	
	public static enum ElevatorStates {
		DOORSOPEN, DOORSCLOSED, MOVINGUP, MOVINGDOWN, STOPPED, TIMEOUT, STUCKOPEN, STUCKCLOSED
	};
	private ElevatorStates currentState;

	/**
	 * Constructor for Elevator
	 * 
	 * @param sch, Scheduler being used
	 */
	public Elevator(int portNum, int assignedNum, boolean timeoutEnabled) {
		
		try {
			this.sendAndReceive = new DatagramSocket();
			if(timeoutEnabled) {
				sendAndReceive.setSoTimeout(15000);
			}			
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.currentFloor = 1; // elevator starts at floor 1
		this.assignedNum = assignedNum;
		this.currentState = ElevatorStates.DOORSCLOSED; // Elevator starts at a closed state
		this.handlerPort = portNum;
		this.errorCode = 0;
	}


	private void sendAndGetMessage(PassStateEvent pse, boolean send) {
		DatagramPacket sending; // both packets
		DatagramPacket receiving;

		try {
			if (send) {
				// Set up byte array streams to turn Message into a byte array
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);

				objectStream.writeObject(pse);
				objectStream.flush();

				byte sendingData[] = byteStream.toByteArray();
				// Send to floor handler
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(), handlerPort);
				sendAndReceive.send(sending);

				byte receivingData[] = new byte[1];
				
				// Get response back, should be only length 1
				receiving = new DatagramPacket(receivingData, 1);
				sendAndReceive.receive(receiving);

				byteStream.close();
				objectStream.close();
			} else {
				byte sendingData[] = new byte[1];
				// send void message to notify that I want a message
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(), handlerPort);
				sendAndReceive.send(sending);

				byte receivingData[] = new byte[MAX_DATA_SIZE];
				
				// Get destination floor from handler
				receiving = new DatagramPacket(receivingData, receivingData.length);
				sendAndReceive.receive(receiving);
 
				if (receivingData[0] != 0) {
					// read as unsigned since byte values over 100 may convert to negative integer value
					int data = Byte.toUnsignedInt(receivingData[0]);
					if(data >= 200) { // TIMEOUT error 
						errorCode = 2;
					} else if (data >= 100) { // DoorsStruck error
						errorCode = 1;
					}
					this.destination = data % 100; // 10s and 1s digit contain destination floor
				} else {
					this.destination = 0;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	@Override
	/**
	 * Run method of the elevator. Gets the floor request from the scheduler for
	 * this specific elevator. The elevator is initially in the stopped state and
	 * will query its floor queue through a call to the Scheduler in order to
	 * determine when it should stop to drop off/pick up people.
	 */
	public void run() {

		Thread tThread = new Thread(new TimeoutTimer(this), "Timer");
		tThread.start();

		while (!Thread.currentThread().isInterrupted()) {
			try {
				checkDoorsStuck();
				// Lets the scheduler know which floor it is on and its state
				sendAndGetMessage(new PassStateEvent(currentFloor, currentState, assignedNum), true);

				// If the elevator has reached its destination or does not have one(0)
				sendAndGetMessage(new PassStateEvent(currentFloor, currentState, assignedNum), false);

				Thread.sleep(100);
				// If timeout occurs, break out and end the thread.
				if (currentState == ElevatorStates.TIMEOUT) {
					sendAndGetMessage(new PassStateEvent(currentFloor, currentState, assignedNum), true);
					sendAndGetMessage(new PassStateEvent(), false);
					Thread.currentThread().interrupt();
				}

				if (destination == currentFloor && currentState != ElevatorStates.DOORSCLOSED) {
					currentState = ElevatorStates.STOPPED;
					Thread.sleep(TIME_DOORS);
					// If there is a destination request for the current floor, the doors open
					currentState = ElevatorStates.DOORSOPEN;

					System.out.println(
							"Elevator " + assignedNum + ": Letting People in and out on floor " + currentFloor + "!");
					
					Thread.sleep(TIME_DOORS);
					this.currentState = ElevatorStates.DOORSCLOSED; // Set to doors closed
				}

				// Interrupt timer because next floor was successfully reached.
				tThread.interrupt();
				if (destination != 0 && errorCode == 0) {
					if (destination > currentFloor) { // check if the elevator needs to move up or down
						currentState = ElevatorStates.MOVINGUP;
						Thread.sleep(TIME_TO_MOVE);
					} else if (destination < currentFloor) {
						currentState = ElevatorStates.MOVINGDOWN;
						Thread.sleep(TIME_TO_MOVE);
					}
					
					if (currentState.equals(ElevatorStates.MOVINGUP)) {
						currentFloor++;
					}
					if (currentState.equals(ElevatorStates.MOVINGDOWN)) {
						currentFloor--;
					}
				} else if(errorCode == 1) {
					currentState = ElevatorStates.STUCKOPEN;
					errorCode = 0;
				} else if(errorCode == 2) { 
					// delay so the TimeoutTimer can interrupt the elevator thread
					Thread.sleep(TIME_TO_MOVE + 1000);
				}
				tThread.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Helper method that checks if the elevator doors are in a stuck state 
	 * If the doors are stuck, gracefully fix and continue
	 * 
	 * @throws InterruptedException
	 */
	private void checkDoorsStuck() throws InterruptedException{
		//If the elevator is stuck, the state will be modified to either DOORSOPEN or DOORSCLOSED respectively. 
		if (currentState == ElevatorStates.STUCKCLOSED) {
			System.out.println("Elevator " + assignedNum + " has its doors stuck closed. Fixing...");
			Thread.sleep(2000);
			currentState = ElevatorStates.DOORSOPEN;
		}
		else if (currentState == ElevatorStates.STUCKOPEN) {
			System.out.println("Elevator " + assignedNum + " has its doors stuck open. Fixing...");
			Thread.sleep(2000);
			currentState = ElevatorStates.DOORSCLOSED;
		}
		errorCode = 0;
	}

	/**
	 * Getter method to access the currentFloor of the Elevator
	 * 
	 * @return int : current floor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * Getter method to access the assignedNum of the elevator
	 * 
	 * @return int : assigned Number
	 */
	public int getAssignedNum() {
		return assignedNum;
	}

	/**
	 * Getter method for the current State of the elevator
	 * 
	 * @return ElevatorStates : the current state of the elevator
	 */
	public ElevatorStates getcurrentState() {
		return currentState;
	}

	/**
	 * Setter method used by Timeout Thread to set elevator state if moving floors takes longer than expected
	 */
	public void setTimeout() {
		this.currentState = ElevatorStates.TIMEOUT;
	}
	
	//For testing purposes
	public void setState(Elevator.ElevatorStates state) {
		this.currentState = state;
	}
	
	/**
	 * Getter method used to retrieve destination floor for testing
	 * @return destinationFloor : int
	 */
	public int getDestinationFloor() {
		return this.destination;
	}
	
	/**
	 * Method used to close sockets for testing purposes
	 */
	public void closeSockets() {
		sendAndReceive.close();
	}


	public static void main(String[] args) {

		Thread e1 = new Thread(new Elevator(69, 1, Scheduler.TIMEOUT_ENABLED), "0");
		Thread e2 = new Thread(new Elevator(70, 2, Scheduler.TIMEOUT_ENABLED), "1");
		Thread e3 = new Thread(new Elevator(71, 3, Scheduler.TIMEOUT_ENABLED), "2");
		Thread e4 = new Thread(new Elevator(72, 4, Scheduler.TIMEOUT_ENABLED), "3");

		e1.start();
		e2.start();
		e3.start();
		e4.start();
	}
}