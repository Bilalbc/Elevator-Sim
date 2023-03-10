package source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * Floor object which reads instructions from file and passes it to the elevator
 * scheduler
 * 
 * @author Kousha Motazedian, Matthew Parker
 * @version 3.0
 * @date March 9th, 2023
 */
public class Floor implements Runnable {
	private File floorRequests;
	private int messageDelay = 0;
	private DatagramSocket sendAndReceive;

	/**
	 * Constructor for Floor class
	 * 
	 * @param sch, Scheduler being used
	 */
	public Floor(File file) {
		this.floorRequests = file;
		try {
			this.sendAndReceive = new DatagramSocket();
			sendAndReceive.setSoTimeout(10000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to create a request message by reading file validates message before
	 * returning
	 * 
	 * @param reader, File reader being used
	 * @return req, Request information, from file null if data was in incorrect
	 *         format
	 */
	private Message createRequest(Scanner reader) {

		String data = reader.nextLine();
		String[] values = data.split(", ");
		Message req = null;

		if (validateRequest(values)) {
			req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));
			messageDelay = Integer.parseInt(values[4]);
		}
		return req;

	}

	/**
	 * Method used to validate the contents of request data. Data must follow format
	 * similar to: 13:02, 2, UP, 4, 500
	 * 
	 * @param request
	 * @return boolean : true if request follows correct format, and false otherwise
	 */
	private boolean validateRequest(String[] request) {

		if (request.length == 5) {
			if (request[0].matches("\\d+:\\d+") && 
					request[1].matches("\\d+") && 
					request[2].equals("UP") || request[2].equals("DOWN") && 
					request[3].matches("\\d+") && 
					request[4].matches("\\d+")) {
				return true;
			}
		}
		System.err.println("An incorrect request was recieved");
		return false;
	}
	
	/**
	 * Send and gets messages through UDP
	 * @param sendingMessage, either null or a Message Object, it is a message object when there is a message to be sent, null when you want to receive a message
	 * @return Message or null, if you passed null, it will return a message. Otherwise it will return null.
	 */
	
	private void sendAndGetMessage(Message sendingMessage, boolean send) {
		DatagramPacket sending; //both packets
		DatagramPacket receiving;
		try {
			if(send) {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); //set up byte array streams to turn Message into a byte array
				ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
				objectStream.writeObject(sendingMessage);
				objectStream.flush();
				byte sendingData[] = byteStream.toByteArray();
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(), 42); //Send to floor handler
				sendAndReceive.send(sending);
				byte receivingData[] = new byte[1];
				receiving = new DatagramPacket(receivingData, 1); //Get response back, should be only length 1
				sendAndReceive.receive(receiving);
				byteStream.close();
				objectStream.close();
			}
			else {
				byte sendingData[] = new byte[1];
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(), 42); //send void message to notify that I want a message
				sendAndReceive.send(sending);
				byte receivingData[] = new byte[50];
				receiving = new DatagramPacket(receivingData, receivingData.length); //get message from handler
				sendAndReceive.receive(receiving);
				ByteArrayInputStream byteStream = new ByteArrayInputStream(receiving.getData()); //unpack into an object
				ObjectInputStream objectStream = new ObjectInputStream(byteStream);
				sendingMessage = (Message) objectStream.readObject();
				byteStream.close();
				objectStream.close();
			}
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}

	@Override
	/**
	 * Run method for Floor class, Scans file using reader, and checks for
	 * instructions, and if there are, that is passed in a message to the scheduler,
	 * and then the reply from the elevator is printed
	 */
	public void run() {
		try {
			Scanner reader = new Scanner(floorRequests);
			while (true) {
				if (reader.hasNextLine()) {

					// if the delay to send the request has elapsed
					if (messageDelay <= 0) {
						Message req = createRequest(reader);
						if (req != null) { // if request was valid
							System.out.println("Passing message: " + req);
							
							sendAndGetMessage(req, true);
							
							
						}
					} 
					else {
						// delay by part of total delay to allow floor to continue to receive replies
						// from the elevator
						messageDelay -= 100;
						Thread.sleep(100);
					}
				} 
				else {
					// notify scheduler that there are no more requests to be sent
					Message done = new Message("done");
					sendAndGetMessage(done, true);
					reader.close();
				}
				Message returned = new Message("");
				sendAndGetMessage(returned, false);
				System.out.println(returned.getReturnMessage());

			}
		} catch (FileNotFoundException | InterruptedException e) {
			System.out.println("filenotfound");
		}
	}
	
	public static void main(String[] args) {
		File file = new File("src//source//Requests.csv");
		Thread f = new Thread(new Floor(file), "Floor");

		f.start();
	}
}
