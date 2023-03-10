package source;

public class PassStateEvent {
	
	private int currentFloor; 
	private Elevator.ElevatorStates currentState;
	private int assignedNum;
	private boolean setClosed;
	
	public PassStateEvent(int currentFloor, Elevator.ElevatorStates currentState, int assignedNum)
	{
		this.currentFloor = currentFloor;
		this.currentState = currentState;
		this.assignedNum = assignedNum;
		this.setClosed = false;
	}
	
	public PassStateEvent(int currentFloor, Elevator.ElevatorStates currentState, int assignedNum, boolean setClosed)
	{
		this.currentFloor = currentFloor;
		this.currentState = currentState;
		this.assignedNum = assignedNum;
		this.setClosed = setClosed;
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
	
	public boolean getClosed()
	{
		return this.setClosed;
	}
}
