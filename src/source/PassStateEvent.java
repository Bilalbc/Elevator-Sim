package source;

import java.io.Serializable;

public class PassStateEvent implements Serializable{
	
	private static final long serialVersionUID = 3332242023946759867L;
	
	private int currentFloor; 
	private Elevator.ElevatorStates currentState;
	private int assignedNum;
	
	public PassStateEvent(int currentFloor, Elevator.ElevatorStates currentState, int assignedNum)
	{	
		this.currentFloor = currentFloor;
		this.currentState = currentState;
		this.assignedNum = assignedNum;
	}
	
	public int getCurrentFloor()
	{
		return this.currentFloor;
	}
	
	public Elevator.ElevatorStates getCurrentState()
	{
		return this.currentState;
	}
	
	public int getAssignedNum()
	{
		return this.assignedNum;
	}
	
	@Override
	public String toString() {
		return "Elevator " + assignedNum + " on Floor " + currentState + ", current state " + currentState;
	}
}
