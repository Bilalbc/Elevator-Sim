package source;

import java.util.ArrayList;
/**
 * An Elevator class that takes requests from the scheduler and passes gives a reply back for the Floor.
 * @author Akshay V., Kousha Motazedian, Matthew Parker
 * @version 2.0
 * @date February 5th, 2023
 *
 */

public class Elevator implements Runnable{

    private int currentFloor; 
    private int assignedNum;
    private Scheduler scheduler;
    private int destination = 0; //current destination floor, 0 means no destination at the moment
    public static enum ElevatorStates {DOORSOPEN, DOORSCLOSED, MOVINGUP, MOVINGDOWN, STOPPED};
    private ElevatorStates currState;
    

    /**
     * Constructor for Elevator
     * @param sch, Scheduler being used
     */
    public Elevator(Scheduler sch) {
        this.scheduler = sch;
        this.currentFloor = 1; //elevator starts at floor 1
        this.assignedNum = 1; //only have 1 elevator right now
        this.currState = ElevatorStates.DOORSCLOSED; //Elevator starts at a closed state
    }

    @Override
    /**
     * Run method of the elevator.
     * Gets the request from the scheduler and prints it out. It creates a reply and gives it back to the scheduler.
     */
    public void run() {
    	while (!scheduler.isClosed()) {
    		
    		if(destination == currentFloor) {
				currState = ElevatorStates.STOPPED;
				currState = ElevatorStates.DOORSOPEN; //If there is a destination request for the current floor, the doors open
				System.out.println("Elevator "+ assignedNum + ": Letting People in and out!");
			}
    		
    		scheduler.passState(currentFloor, currState, assignedNum); //Lets the scheduler know which floor it is on and its state
    		
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    		
    		if(currentFloor == destination || destination == 0) { //If the elevator has reached its destination or does not have one (0)
    			
    			int newDestination = scheduler.readMessage(); //get new destination from sch
    			if(newDestination != 0) { //If the new destination is not 0 (meaning no destinations left), the elevator takes on the new destination
    				destination = newDestination;
    			}
    			if(currentFloor == destination) { //If the destination still has not changed (because readMessage returned 0) close the system.
    				scheduler.setClosed();
    			}
    		}
    		
            this.currState = ElevatorStates.DOORSCLOSED; //Set to doors closed
            
            if(destination != 0) {
	            if(destination > currentFloor) { // check if the elevator needs to move up or down
	            	currState = ElevatorStates.MOVINGUP;
	            }
	            else {
	            	currState = ElevatorStates.MOVINGDOWN;
	            }
	            
	            //Moves up or down depending on the State of the elevator
	            if(currState.equals(ElevatorStates.MOVINGUP)) {
	            	currentFloor++;
	            }
	            if(currState.equals(ElevatorStates.MOVINGDOWN)) {
	            	currentFloor--;
	            }
	            
            }
    		
    	}
    	
    	System.exit(0);
    }

}
