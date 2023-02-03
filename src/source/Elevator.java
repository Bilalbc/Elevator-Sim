package source;

import java.util.ArrayList;

public class Elevator implements Runnable{

	private int currentFloor; 
	private int assignedNum;
	private Scheduler scheduler;
	private ArrayList<Message> requestQueue;
	
	public Elevator(Scheduler sch, Message message, int num) {
		this.scheduler = sch;
		requestQueue = new ArrayList<>();
		this.assignedNum = num;
	}
	
	private void moveFloor() {
		System.out.println("Current Floor: " + currentFloor);
		
		if (requestQueue != null) {
			
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	

}
