package source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FloorHandler implements Runnable{
	private Scheduler scheduler;
	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket, sendPacketAck;
	private boolean send;
	
	public static final int MAX_DATA_SIZE = 100;
	public static final int TIMEOUT = 40000; //placeholder value for now
	
	public static final int FLOOR_HANDLER_PORT = 42;
	
	public FloorHandler(Scheduler scheduler)
	{
		this.scheduler = scheduler;
		this.send = true;

	    try {
	        socket = new DatagramSocket(FLOOR_HANDLER_PORT);
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
	
	private void passMessage(byte data[])
	{
	    ByteArrayInputStream bis = new ByteArrayInputStream(data);
	    ObjectInput input = null;
	    Object o = null;
	    
	    try {
			input = new ObjectInputStream(bis);
			o = input.readObject();
			input.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
				
	    scheduler.passMessage((Message) o);
	}
	
	private byte[] serializeReply()
	{
		Message m = scheduler.readReply();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(m);
			out.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] returnByte = bos.toByteArray();

		return returnByte;
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
			replyData[0]= (byte) 1;
			
			passMessage(data);
		    		    
		    this.send = false;
		}
		else
		{
		    replyData = new byte[ElevatorHandler.MAX_DATA_SIZE];
		    replyData = serializeReply();
		    		    
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

