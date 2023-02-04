package source;

import java.util.ArrayList;
/**
 * An Elevator class that takes requests from the scheduler and passes gives a reply back for the Floor.
 * @author Akshay V.
 * @version 1.0
 * @date February 5th, 2023
 *
 */

public class Elevator implements Runnable{

    private int currentFloor; 
    private int assignedNum;
    private Scheduler scheduler;
    private ArrayList<Message> requestQueue;

    /**
     * Constructor for Elevator
     * @param sch, Scheduler being used
     */
    public Elevator(Scheduler sch) {
        this.scheduler = sch;
        this.currentFloor = 1;
        requestQueue = new ArrayList<>();
        this.assignedNum = 1; //only have 1 elevator right now
    }

    @Override
    /**
     * Run method of the elevator.
     * Gets the request from the scheduler and prints it out. It creates a reply and gives it back to the scheduler.
     */
    public void run() {
    	while (!scheduler.isClosed()) {
    		Message req = scheduler.readMessage();
            System.out.println("Recieved message: " + req);
    		
            System.out.println("Passing reply: " + req);
            req.setReturnMessage("Arrived at Destination Floor - Request was: " + req);
    		scheduler.passReply(req); 
    		
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }

}
