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
    private ArrayList<Integer> destinationQueue;
    public static enum ElevatorStates {DOORSOPEN, DOORSCLOSED, MOVINGUP, MOVINGDOWN, STOPPED};
    private ElevatorStates currState;
    

    /**
     * Constructor for Elevator
     * @param sch, Scheduler being used
     */
    public Elevator(Scheduler sch) {
        this.scheduler = sch;
        this.currentFloor = 1;
        destinationQueue = new ArrayList<>();
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
    		this.currState = ElevatorStates.DOORSCLOSED; //Door starts off closed
    		if(!(destinationQueue.isEmpty())){
	    		for(int i : destinationQueue) {
	    			if(i == currentFloor) {
	    				currState = ElevatorStates.STOPPED;
	    				currState = ElevatorStates.DOORSOPEN; //If there is a destination request for the current floor, the doors open
	    			}
	    		}
    		}
    		
    		destinationQueue.clear();
    		destinationQueue = scheduler.readMessage();
            System.out.println("Recieved Destinations");
            this.currState = ElevatorStates.DOORSCLOSED;
            
            if(destinationQueue.get(0) > currentFloor) { // check if the elevator needs to move up or down
            	currState = ElevatorStates.MOVINGUP;
            }
            else {
            	currState = ElevatorStates.MOVINGDOWN;
            }
            
            System.out.println("Elevator is Moving");
            //Moves up or down depending on the State of the elevator
            if(currState.equals(ElevatorStates.MOVINGUP)) {
            	currentFloor++;
            }
            if(currState.equals(ElevatorStates.MOVINGDOWN)) {
            	currentFloor--;
            }
            
             //Elevator reached a floor
            
            scheduler.passState(currentFloor, currState);
    		
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }

}
