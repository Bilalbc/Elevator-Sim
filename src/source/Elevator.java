package source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Akshay V., Kousha Motazedian, Matthew Parker
 * @Date: 2023-03-09
 * @Version 3.0
 *
 *       An Elevator class that receives floor requests from the scheduler which
 *       indicates the order in which the elevator should pick up/drop off
 *       riders. As of Iteration2, the elevator follows the states of being able
 *       to move up, move down, stop, close doors and open doors.
 */

public class Elevator implements Runnable {

	private int currentFloor;
	private int assignedNum;
	private int destination = 0; // current destination floor, 0 means no destination at the moment
	private int handlerPort;
	private DatagramSocket sendAndReceive;

	public static enum ElevatorStates {
		DOORSOPEN, DOORSCLOSED, MOVINGUP, MOVINGDOWN, STOPPED
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
			//sendAndReceive.setSoTimeout(10000);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.currentFloor = 1; // elevator starts at floor 1
		this.assignedNum = assignedNum;
		this.currentState = ElevatorStates.DOORSCLOSED; // Elevator starts at a closed state
		this.handlerPort = portNum;
	}
	
	private void sendAndGetMessage(PassStateEvent pse, boolean send) {
		DatagramPacket sending; //both packets
		DatagramPacket receiving;
		
		try {
			if(send) {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); //set up byte array streams to turn Message into a byte array
				ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
				objectStream.writeObject(pse);
				objectStream.flush();
				byte sendingData[] = byteStream.toByteArray();
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(), handlerPort); //Send to floor handler
				sendAndReceive.send(sending);
				byte receivingData[] = new byte[1];
				receiving = new DatagramPacket(receivingData, 1); //Get response back, should be only length 1
				sendAndReceive.receive(receiving);
				byteStream.close();
				objectStream.close();
			}
			else {
				byte sendingData[] = new byte[1];
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(), handlerPort); //send void message to notify that I want a message
				sendAndReceive.send(sending);
				byte receivingData[] = new byte[50];
				receiving = new DatagramPacket(receivingData, receivingData.length); //get message from handler
				sendAndReceive.receive(receiving);

				if(receivingData[0] != 0) {
					this.destination = receivingData[0]; // can maybe make this a parameter to the function and pass by refreence 
					System.out.println("Elevator " + this.assignedNum + " New destination: " + destination);
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
	
		while(true) {
			try {
				if (destination == currentFloor && currentState != ElevatorStates.DOORSCLOSED) {
					currentState = ElevatorStates.STOPPED;
					Thread.sleep(1000);
					// If there is a destination request for the current floor, the doors open
					currentState = ElevatorStates.DOORSOPEN;

					System.out.println(
							"Elevator " + assignedNum + ": Letting People in and out on floor " + currentFloor + "!");
					Thread.sleep(1000);
					this.currentState = ElevatorStates.DOORSCLOSED; // Set to doors closed
				}

				// Lets the scheduler know which floor it is on and its state
				sendAndGetMessage(new PassStateEvent(currentFloor,currentState, assignedNum), true);
				Thread.sleep(1000);
				// If the elevator has reached its destination or does not have one (0)
				sendAndGetMessage(new PassStateEvent(currentFloor,currentState, assignedNum), false);
				if (currentFloor == destination || destination == 0) {
//					System.out.println(this.assignedNum + " Arrived at Destination "+ currentFloor);
//					// If the new destination is not 0 (meaning no destinations left), the elevator
//					// takes on the new destination
//					if (newDestination != 0) {
//						destination = newDestination;
//					}
					/*
					 
					// If the destination still has not changed (because readMessage returned 0),
					// floor is not sending anymore requests, and the scheduler is holding no more
					// requests for the elevator, close the system.
					if (currentFloor == destination && scheduler.isRequestsComplete()
							&& scheduler.getElevatorQueueSize() == 0) {

						// Pass state to ensure floor is not waiting forever
						scheduler.passState(currentFloor, currentState, assignedNum);
						scheduler.setClosed();
					}
					*/
				}

				if (destination != 0) {
					if (destination > currentFloor) { // check if the elevator needs to move up or down
						currentState = ElevatorStates.MOVINGUP;
					} else if (destination < currentFloor) {
						currentState = ElevatorStates.MOVINGDOWN;
					}

					Thread.sleep(1000);
					if (currentState.equals(ElevatorStates.MOVINGUP)) {
						currentFloor++;
					}
					if (currentState.equals(ElevatorStates.MOVINGDOWN)) {
						currentFloor--;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	
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
	
	public static void main(String[] args) {
		
		Thread evt = new Thread(new Elevator(69, 1));
		
		evt.start();
	}

}
