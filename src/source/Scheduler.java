/*
 * @Author: Mohamed Kaddour
<<<<<<< HEAD
 * @Date: 2023-02-04
 * @Version 1.0
 * 
 * As for Iteration1 and Version 1.0, Scheduler class acts as a buffer to transfer a message of type Message between both Floor and 
 * Elevator. The scheduler only holds one item. 
=======

 * @Date: 2023-02-04
 * @Version 1.0
 * 
 * As for Iteration1 and Version 1.0, Scheduler class acts as a buffer to transfer a message of type Message between both Floor and 
 * Elevator. The scheduler manages a queue that, for this iteration, will only hold up to 1 message. The message will then be passed
 * along to either the Floor class or the Elevator depending on the method call. 
>>>>>>> refs/remotes/origin/Iteration1
 * 
 * */
package source;

import java.util.ArrayList;

public class Scheduler {
	
	public static final int REPLY_BUFFER_SIZE = 1;
<<<<<<< HEAD
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
	
=======
	public static final int MESSAGE_BUFFER_SIZE = 1;
	public static final int BUFFER_EMPTY = 0;
	
	/*For now, these will have a maximum size of 1*/
	private ArrayList<Message> messageQueue;
	private ArrayList<Message> replyQueue;
	
	/*
	 * Constructor for class Scheduler. Initializes both the messageQueue and the replyQueue ArrayLists
	 * **/
	public Scheduler()
	{
		this.messageQueue = new ArrayList<>();
		this.replyQueue = new ArrayList<>();
	}
	
	/*
	 * Synchronized method that takes in a message and, if the message queue is not full (size of 1), then it adds 
	 * the message to the queue. 
	 *
	 *@param message of type Message to pass
	 **/
	public synchronized void passMessage(Message message)
	{
		while ((this.messageQueue.size() == MESSAGE_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.messageQueue.add(message);
		
		notifyAll();	
	}
	
	/**
	 * Synchronized method that returns a Message from the queue only if the queue is not empty, else it will wait. 
	 * @param none
	 * @return Message to be read
	 * */
	public synchronized Message readMessage()
	{
		Message message;
		
		while(this.messageQueue.size() != MESSAGE_BUFFER_SIZE)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		message = this.messageQueue.get(BUFFER_EMPTY);
		this.messageQueue.clear(); //Since there is only one item expected at all times, can just clear queue to empty it. 
		
		notifyAll();
		
		return message;
	}

	/*
	 * Synchronized method that takes in a message and, if the reply queue is not full (size of 1), then it adds 
	 * the message to the queue. 
	 *
	 *@param message of type Message to reply
	 **/
	public synchronized void passReply(Message reply)
	{
		while ((this.replyQueue.size() == REPLY_BUFFER_SIZE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.replyQueue.add(reply);
		
		notifyAll();	
	}
	
	/**
	 * Synchronized method that returns a Message from the queue only if the queue is not empty, else it will wait. 
	 * @param none
	 * @return Message to reply
	 * */
>>>>>>> refs/remotes/origin/Iteration1
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
