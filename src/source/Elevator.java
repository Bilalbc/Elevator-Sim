package source;

import java.util.ArrayList;

public class Elevator implements Runnable{

    private int currentFloor; 
    private int assignedNum;
    private Scheduler scheduler;
    private ArrayList<Message> requestQueue;

    public Elevator(Scheduler sch) {
        this.scheduler = sch;
        this.currentFloor = 1;
        requestQueue = new ArrayList<>();
        this.assignedNum = 1; //only have 1 elevator right now
    }

    @Override
    public void run() {
    	while (!scheduler.isClosed()) {
    		Message req = scheduler.readMessage();
            System.out.println("Recieved message: " + req);
    		
    		//receiveRequest();
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
