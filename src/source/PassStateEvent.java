/**
 * @Author: Mohamed Kaddour

 * @Date: 2023-03-25
 * @Version 4.0
 * 
 * Even class that is used to encapsulate several values that are meant to be sent over UDP to the scheduler. 
 * Used to help with serialization. 
 */

package source;

import java.io.Serializable;

public class PassStateEvent implements Serializable {

	private static final long serialVersionUID = 3332242023946759867L;

	private int currentFloor;
	private Elevator.ElevatorStates currentState;
	private int assignedNum;
	private boolean error;

	public PassStateEvent() {
		this.error = true;
	}

	/**
	 * Constructor that initializes the object state.
	 * 
	 * @param currentFloor of type in
	 * @param currentState of type Elevator.ElevatorStates
	 * @param assignedNum  int
	 */
	public PassStateEvent(int currentFloor, Elevator.ElevatorStates currentState, int assignedNum) {
		this.currentFloor = currentFloor;
		this.currentState = currentState;
		this.assignedNum = assignedNum;
		this.error = false;
	}

	/**
	 * Getter for currentFloor
	 * 
	 * @return int
	 */
	public int getCurrentFloor() {
		return this.currentFloor;
	}

	/**
	 * Getter for currentState
	 * 
	 * @return Elevator.ElevatorStates
	 */
	public Elevator.ElevatorStates getCurrentState() {
		return this.currentState;
	}

	/**
	 * Getter for assignedNum
	 * 
	 * @return int
	 */
	public int getAssignedNum() {
		return this.assignedNum;
	}

	/**
	 * Getter for error state
	 * 
	 * @return error : boolean representing elevator error state
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Override to String method
	 */
	@Override
	public String toString() {
		return "Elevator " + assignedNum + " on Floor " + currentFloor + ", current state " + currentState;
	}

}
