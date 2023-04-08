package source;

import source.Elevator.ElevatorStates;

public interface ElevatorView {
	
	public void updateFloorAndState(int currentFloor, int elevatorNum, ElevatorStates elevatorState);
	
	public void updateLight();
	
	public void updateFloorRequest(int startFloor);

}
