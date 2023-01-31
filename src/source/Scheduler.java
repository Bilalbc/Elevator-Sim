/*
 * @Author: Mohamed Kaddour
 * @Date: 2023-02-04
 * @Version 1.0
 * 
 * As for Iteration1 and Version 1.0, Scheduler class acts as a buffer to transfer a message of type Request between both Floor and 
 * Elevator. The scheduler only holds one item. 
 * 
 * */
package source;

import java.util.ArrayList;

public class Scheduler {
	
	public static final int REPLY_BUFFER_SIZE = 1;
	public static final int REQUEST_BUFFER_SIZE = 1;
	public static final int BUFFER_EMPTY = 0;
	
	/*For now, these will have a maximum size of 1*/
	private ArrayList<Request> requestQueue;
	private ArrayList<Request> replyQueue;
	
	public Scheduler()
	{
		this.requestQueue = new ArrayList<>();
		this.replyQueue = new ArrayList<>();
	}
	
	public synchronized void passRequest(Request request)
	{
		while ((this.requestQueue.size() == REQUEST_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.requestQueue.add(request);
		
		notifyAll();	
	}
	
	public synchronized Request readRequest()
	{
		Request request;
		
		while(this.requestQueue.size() != REQUEST_BUFFER_SIZE)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		request = this.requestQueue.get(BUFFER_EMPTY);
		this.requestQueue.clear();
		
		notifyAll();
		
		return request;
	}
	
	public synchronized void passReply(Request request)
	{
		while ((this.replyQueue.size() == REPLY_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.replyQueue.add(request);
		
		notifyAll();	
	}
	
	public synchronized Request readReply()
	{
		Request reply;
		
		while(this.replyQueue.size() != REPLY_BUFFER_SIZE)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		reply = this.replyQueue.get(BUFFER_EMPTY);
		this.replyQueue.clear();
		
		notifyAll();
		
		return reply;
	}
	
	
}
