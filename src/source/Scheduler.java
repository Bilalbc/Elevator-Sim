/*
 * @Author: Mohamed Kaddour
 * @Date: 2023-02-04
 * @Version 1.0
 * 
 * As for Iteration1 and Version 1.0, Scheduler class acts as a buffer to transfer a message of type Message between both Floor and 
 * Elevator. The scheduler only holds one item. 
 * 
 * */
package source;

import java.util.ArrayList;

public class Scheduler {
	
	public static final int REPLY_BUFFER_SIZE = 1;
	public static final int Message_BUFFER_SIZE = 1;
	public static final int BUFFER_EMPTY = 0;
	
	/*For now, these will have a maximum size of 1*/
	private ArrayList<Message> MessageQueue;
	private ArrayList<Message> replyQueue;
	
	public Scheduler()
	{
		this.MessageQueue = new ArrayList<>();
		this.replyQueue = new ArrayList<>();
	}
	
	public synchronized void passMessage(Message Message)
	{
		while ((this.MessageQueue.size() == Message_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.MessageQueue.add(Message);
		
		notifyAll();	
	}
	
	public synchronized Message readMessage()
	{
		Message Message;
		
		while(this.MessageQueue.size() != Message_BUFFER_SIZE)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		Message = this.MessageQueue.get(BUFFER_EMPTY);
		this.MessageQueue.clear();
		
		notifyAll();
		
		return Message;
	}
	
	public synchronized void passReply(Message Message)
	{
		while ((this.replyQueue.size() == REPLY_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.replyQueue.add(Message);
		
		notifyAll();	
	}
	
	public synchronized Message readReply()
	{
		Message reply;
		
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
