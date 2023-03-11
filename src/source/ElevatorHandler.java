/**
 * @Author: Mohamed Kaddour

 * @Date: 2023-03-11
 * @Version 3.0
 * 
 * Interface thread to interact with its respective socket. Takes the message and then sends and receives the reply message from the scheduler. 
 * Sends an ack to the Elevator thread and waits to see that it's ready to receive. 
 */

package source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ElevatorHandler implements Runnable{
	
	private Scheduler scheduler;
	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	private boolean send;
	
	public static final int MAX_DATA_SIZE = 250;
	public static final int TIMEOUT = 40000; //placeholder value for now

	/**
	 * ElevatorHandler constructor that that takes in the scheduler and initializes the socket. Also sets the send condition to true initially.
	 * 
	 * @param scheduler, Scheduler to interact with
	 * @param elevatorPort, of type int, the port to initialize the socket to. 
	 * */
	public ElevatorHandler(Scheduler scheduler, int elevatorPort)
	{
		this.scheduler = scheduler;
		this.send = true;

	    try {
	        socket = new DatagramSocket(elevatorPort);
	        socket.setSoTimeout(TIMEOUT);
	     } catch (SocketException se) {
	        se.printStackTrace();
	        System.exit(1);
	     }
	}

	/**
	 * Thread run method that essentially runs the sendAndReceieve() method.
	 * */
	@Override
	public void run() {
		while (true)
		{
			sendAndReceieve();
		}
	}
	
	/**
	 * Helper method that deserializes a byte stream to type PassStateEvent and passes it to the scheduler. 
	 * @param data, byte array
	 * */
	private void passStateHandler(byte data[])
	{
	    ByteArrayInputStream bis = new ByteArrayInputStream(data);
	    ObjectInput input = null;
	    Object o = null;
	    
		try {
			input = new ObjectInputStream(bis);
			o = input.readObject();
			bis.close();
			input.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PassStateEvent pse = (PassStateEvent) o;
		
	    scheduler.passState(pse);
	}
	
	/**
	 * Performs both the sending and, either sending a reply, or an acknowledgement depending on the 
	 * send state. 
	 * */
	private void sendAndReceieve()
	{
		//Initialize the data and the replyData byte arrays. 
		byte data[] = new byte[MAX_DATA_SIZE];
		byte replyData [];
		
		//Always consistently receive packet of size data and then receive on the packet.  
		receivePacket = new DatagramPacket(data, data.length);
		
	    try {        
		   	socket.receive(receivePacket);
	
			if (send)
			{	
				//If send is true, then the replyData is the acknowledgment message, which is just a byte array of size 1. 
				replyData = new byte[1];
				replyData[0]= (byte) 1;
				
			    passStateHandler(data);
			    		    
			    this.send = false;
			}
			else
			{
				//If send is false, then the reply is the actual reply from the scheduler, thus serialize reply.
			    replyData = new byte[1];
			    replyData[0] = (byte) scheduler.readMessage();
			    		    
			    this.send = true;
			}
			
			//Always send the packet at the end back for the reply. 
		    sendPacket = new DatagramPacket(replyData, replyData.length, receivePacket.getAddress(), receivePacket.getPort());
			socket.send(sendPacket);
		} catch (IOException e) {
		   System.out.print("IO Exception: likely:");
		   System.out.println("Receive Socket Timed Out.\n" + e);
		   e.printStackTrace();
		   System.exit(1);
		}
	}
}
