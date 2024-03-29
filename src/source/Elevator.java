package source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

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
	private boolean carryingPassengers;
	private DatagramSocket sendAndReceive;

	private boolean[] lightsGrid;

	private static final int NUM_FLOORS = 10;
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
	public Elevator(int portNum, int assignedNum) {
		try {
			this.sendAndReceive = new DatagramSocket();
			sendAndReceive.setSoTimeout(10000);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.currentFloor = 1; // elevator starts at floor 1
		this.assignedNum = assignedNum;
		this.currentState = ElevatorStates.DOORSCLOSED; // Elevator starts at a closed state
		this.handlerPort = portNum;
		this.carryingPassengers = false;
		this.lightsGrid = new boolean[NUM_FLOORS];
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
				// Get destination queue from handler

				receiving = new DatagramPacket(receivingData, receivingData.length);
				sendAndReceive.receive(receiving);

				// unpack into an object
				ByteArrayInputStream byteStream = new ByteArrayInputStream(receiving.getData());

				ObjectInputStream objectStream = new ObjectInputStream(byteStream);

				ArrayList<Integer> temp = new ArrayList<Integer>();
				
				try {
					temp = (ArrayList<Integer>) objectStream.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				if(carryingPassengers) {
					for(int i = 1; i < temp.size(); i++) {
						if(i > 0) {
							lightsGrid[temp.get(i-1)] = true;											
						}
					}
				}

				byteStream.close();
				objectStream.close();
				
				if(temp.size() > 0) {
					this.destination = temp.get(0);
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

				// If timeout occurs, break out and end the thread.
				if (currentState == ElevatorStates.TIMEOUT) {
					Thread.currentThread().interrupt();
					Thread.sleep(200);
					
					sendAndGetMessage(new PassStateEvent(currentFloor, currentState, assignedNum), true);
					continue;
				}

				Thread.sleep(1000);
				if (destination == currentFloor && currentState != ElevatorStates.DOORSCLOSED) {
					currentState = ElevatorStates.STOPPED;
					Thread.sleep(TIME_DOORS);
					// If there is a destination request for the current floor, the doors open
					currentState = ElevatorStates.DOORSOPEN;

					System.out.println(
							"Elevator " + assignedNum + ": Letting People in and out on floor " + currentFloor + "!");
					
//					if(!carryingPassengers) {
//						carryingPassengers = true;
//					} else if (carryingPassengers && !hasDestinations()) {
//						carryingPassengers = false;
//					}
					lightsGrid[currentFloor-1] = false;
					Thread.sleep(TIME_DOORS);
					this.currentState = ElevatorStates.DOORSCLOSED; // Set to doors closed
				}

				// Interrupt timer because next floor was successfully reached.
				tThread.interrupt();
				if (destination != 0) {

					if (destination > currentFloor) { // check if the elevator needs to move up or down
						currentState = ElevatorStates.MOVINGUP;
					} else if (destination < currentFloor) {
						currentState = ElevatorStates.MOVINGDOWN;
					} else {
						System.out.println("Elevator" + assignedNum + " is already on destination floor");
					}
					Thread.sleep(TIME_TO_MOVE);
					if (currentState.equals(ElevatorStates.MOVINGUP)) {
						currentFloor++;
					}
					if (currentState.equals(ElevatorStates.MOVINGDOWN)) {
						currentFloor--;
					}
				}
				System.out.println(
						"Elevator " + assignedNum + " is on floor " + currentFloor + ". Destination is " + destination);

				for(boolean i : lightsGrid) {
					System.out.print(i + ", ");
				}
				System.out.println();
				
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
	}

	/**
	 * Helper method to see if there are any destinations
	 * Used to determine if there are any passengers on board
	 * 
	 * @return boolean : If passengers are on board 
	 */
	private boolean hasDestinations() {
		for(boolean i : lightsGrid) {
			if(i) return true;
		}
		return false;
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

	public void setTimeout() {
		this.currentState = ElevatorStates.TIMEOUT;
	}
	
	//For testing purposes
	public void setState(Elevator.ElevatorStates state) {
		this.currentState = state;
	}

	public static void main(String[] args) {

		Thread e1 = new Thread(new Elevator(69, 1), "0");
		Thread e2 = new Thread(new Elevator(70, 2), "1");
		Thread e3 = new Thread(new Elevator(71, 3), "2");
		Thread e4 = new Thread(new Elevator(72, 4), "3");

		e1.start();
		e2.start();
		e3.start();
		e4.start();
	}

}