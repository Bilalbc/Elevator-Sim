package source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ElevatorHandler implements Runnable{
	
	private int elevatorPort;
	private Scheduler scheduler;
	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	private boolean send;
	
	public static final int MAX_DATA_SIZE = 100;
	public static final int TIMEOUT = 40000; //placeholder value for now

	
	public ElevatorHandler(int elevatorPort, Scheduler scheduler)
	{
		this.elevatorPort = elevatorPort;
		this.scheduler = scheduler;
		this.send = true;

	    try {
	        socket = new DatagramSocket(this.elevatorPort);
	        socket.setSoTimeout(TIMEOUT);
	     } catch (SocketException se) {
	        se.printStackTrace();
	        System.exit(1);
	     }
	}

	@Override
	public void run() {
		
		while (true)
		{
			sendAndReceieve();
		}
	}
	
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
	
	private void sendAndReceieve()
	{
		byte data[] = new byte[MAX_DATA_SIZE];
		byte replyData [];
		
		receivePacket = new DatagramPacket(data, data.length);
		
	    try {        
	   	 socket.receive(receivePacket);
	    } catch (IOException e) {
	       System.out.print("IO Exception: likely:");
	       System.out.println("Receive Socket Timed Out.\n" + e);
	       e.printStackTrace();
	       System.exit(1);
	    }

		if (send)
		{	
			replyData = new byte[1];
			replyData[0]= (byte) 7;
			
		    passStateHandler(data);
		    		    
		    this.send = false;
		}
		else
		{
		    replyData = new byte[1];
		    replyData[0] = (byte) scheduler.readMessage();
		    		    
		    this.send = true;
		}
		
	    sendPacket = new DatagramPacket(replyData, replyData.length, receivePacket.getAddress(), receivePacket.getPort());

	    try {        
		socket.send(sendPacket);
		} catch (IOException e) {
		   System.out.print("IO Exception: likely:");
		   System.out.println("Receive Socket Timed Out.\n" + e);
		   e.printStackTrace();
		   System.exit(1);
		}
	}
}
