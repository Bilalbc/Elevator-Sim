/**
 * @Author: Mohamed Kaddour
 * @Date: 2023-04-12
 * @Version 5.0
 * 
 * ElevatorView Interface that is used to indicate how each view is to be updated using the method signatures. In this case, there is 
 * only one view
 */

package source;

import source.Elevator.ElevatorStates;

public interface ElevatorView {
	
	/**
	 * Updates the floor location of the specific elevator and also the state of the elevator.
	 * 
	 * @param currentFloor int
	 * @param elevatorNum int
	 * @param elevatorState ElevatorStates
	 * */
	public void updateFloorAndState(int currentFloor, int elevatorNum, ElevatorStates elevatorState);
	
    /**
     * Updates the light for the specified elevator indicating that either a request is ready at that floor or that floor is a destination.
     * The light is then either turned on or off depending on the passed in boolean
     * 
     * @param floorLight int
     * @param elevatorNum int
     * @param turnOn boolean
     * */
	public void updateLight(int floorLight, int elevatorNum, boolean turnOn);
	
	/**
	 * Flashes the specified floor when there is a request there and then flashes it off when another request appears.
	 * 
	 * @param startFloor int
	 * */
	public void updateFloorRequest(int startFloor);
}
