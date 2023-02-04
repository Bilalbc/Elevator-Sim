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

    private synchronized void receiveRequest() {
        requestQueue.add(scheduler.readMessage());

        Message msg = requestQueue.get(0);

        System.out.println(msg);
    }

    @Override
    public void run() {
        receiveRequest();
        scheduler.passReply(requestQueue.get(0));
    }

}
