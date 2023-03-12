/**
 * @Author: Mohamed Kaddour

 * @Date: 2023-03-11
 * @Version 3.0
 * 
 * Interface thread to interact with the Floor system. Takes the message and then sends and receives the reply message from the scheduler. 
 * Sends an ack to Floor subsystem and waits to see that it's ready to receive. 
 */

package source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FloorHandler implements Runnable {
	private Scheduler scheduler;
	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	private boolean send;

	public static final int MAX_DATA_SIZE = 250;
	public static final int TIMEOUT = 40000; // placeholder value for now

	public static final int FLOOR_HANDLER_PORT = 42;

	/**
	 * FloorHandler constructor that that takes in the scheduler and initializes the
	 * socket. Also sets the send condition to true initially.
	 * 
	 * @param scheduler, Scheduler to interact with
	 */
	public FloorHandler(Scheduler scheduler) {
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

	/**
	 * Thread run method that essentially runs the sendAndReceieve() method.
	 */
	@Override
	public void run() {
		while (true) {
			sendAndReceieve();
		}
	}

	/**
	 * Helper method that deserializes a byte stream to type Message and passes it
	 * to the scheduler.
	 * 
	 * @param data, byte array
	 */
	private void passMessage(byte data[]) {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream input = null;
		Object o = null;

		try {
			input = new ObjectInputStream(bis);

			o = input.readObject();
			input.close();
			bis.close();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		if ((((Message) o).getReturnMessage() != null) && ((Message) o).getReturnMessage().equals("done")) {
			scheduler.setRequestsComplete(true);
		} else {
			scheduler.passMessage((Message) o);
		}
	}

	/**
	 * Helper method that serializes the reply in order to send it back to the
	 * floor.
	 * 
	 * @return byte[] array that is serialized Message object.
	 */
	private byte[] serializeReply() {
		Message m = scheduler.readReply();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(m);
			out.flush();
			out.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] returnByte = bos.toByteArray();

		return returnByte;
	}

	/**
	 * Performs both the sending and, either sending a reply, or an acknowledgement
	 * depending on the send state.
	 */
	private void sendAndReceieve() {
		// Initialize the data and the replyData byte arrays.
		byte data[] = new byte[MAX_DATA_SIZE];
		byte replyData[];

		// Always consistently receive packet of size data and then receive on the
		// packet.
		receivePacket = new DatagramPacket(data, data.length);

		try {
			socket.receive(receivePacket);

			if (send) {
				// If send is true, then the replyData is the acknowledgment message, which is
				// just a byte array of size 1.
				replyData = new byte[1];
				replyData[0] = (byte) 1;

				// Handle the passMessage
				passMessage(data);

				this.send = false;
			} else {
				// If send is false, then the reply is the actual reply from the scheduler, thus
				// serialize reply.
				replyData = new byte[ElevatorHandler.MAX_DATA_SIZE];
				replyData = serializeReply();

				this.send = true;
			}

			// Always send the packet at the end back for the reply.
			sendPacket = new DatagramPacket(replyData, replyData.length, receivePacket.getAddress(),
					receivePacket.getPort());

			socket.send(sendPacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
}
