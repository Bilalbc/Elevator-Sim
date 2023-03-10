package source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FloorHandler {
	private Scheduler scheduler;
	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	
	public static final int MAX_DATA_SIZE = 100;
	public static final int TIMEOUT = 40000; //placeholder value for now
	
	public static final int FLOOR_HANDER_PORT = 42;

	
	public FloorHandler(Scheduler scheduler)
	{
		this.scheduler = scheduler;

	    try {
	        socket = new DatagramSocket();
	        socket.setSoTimeout(TIMEOUT);
	     } catch (SocketException se) {
	        se.printStackTrace();
	        System.exit(1);
	     }
	}

	@Override
	public void run() {
		byte data[] = new byte[MAX_DATA_SIZE];
		receivePacket = new DatagramPacket(data, data.length);
		
	    try {        
	   	 socket.receive(receivePacket);
	    } catch (IOException e) {
	       System.out.print("IO Exception: likely:");
	       System.out.println("Receive Socket Timed Out.\n" + e);
	       e.printStackTrace();
	       System.exit(1);
	    }
	    
	    passStateHandler(data);
	    
	    byte replyData[] = new byte[1];
	    replyData[0] = (byte) scheduler.readMessage();
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
	
	private void passStateHandler(byte data[])
	{
	    ByteArrayInputStream bis = new ByteArrayInputStream(data);
	    ObjectInput input = null;
	    
	    try {
			input = new ObjectInputStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    Object o = null;
	    
		try {
			o = input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PassStateEvent pse = (PassStateEvent) o;
		
	    scheduler.passState(pse);
	}
}

