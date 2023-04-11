package source;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * Floor object which reads instructions from file and passes it to the elevator
 * scheduler
 * 
 * @author Kousha Motazedian, Matthew Parker
 * @version 4.0
 * @date March 25th, 2023
 */
public class Floor implements Runnable {
	private File floorRequests;
	private long messageDelay = 0;
	private DatagramSocket sendAndReceive;
	
	// shared data. not synchronized since minimum delay between requests will ensure it is not accessed twice in the same momoent 
	private ArrayList<Message> requestQueue;
	private boolean requestsComplete;
	private Message latestReturned; // for testing

	private Scanner scanner;
	private int requestsHandled; // Value for tracking requests read for testing
	private boolean scannerActive;
	
	
	/**
	 * Utility constructor to allow access of methods in testing
	 */
	public Floor() {
		// Do nothing 
	}
	
	/**
	 * Constructor for Floor class
	 * 
	 * @param sch, Scheduler being used
	 */
	public Floor(File file) {
		this.floorRequests = file;
		this.requestQueue = new ArrayList<Message>();
		this.requestsComplete = false;
		this.requestsHandled = 0;
		try {
			this.sendAndReceive = new DatagramSocket();
			sendAndReceive.setSoTimeout(15000);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		updateTimes();
	}

	/**
	 * Helper method to randomly recalculate the time values for incoming requests.
	 * On average the requests will extend over a 3 minute period.
	 * 
	 * Used to ensure the request times will be set based on time the program is run
	 */
	private void updateTimes() {
		ArrayList<String[]> rows = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(floorRequests));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] row = line.split(",");
				rows.add(row);
			}

			// Modify the first column of each row
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			for (String[] row : rows) {
				//int rand = (int) (Math.random() * 10) + 4;
				int rand = 1;
				cal.add(Calendar.SECOND, rand);
				row[0] = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
						+ cal.get(Calendar.SECOND);
			}

			reader.close();
			
			// Write out adjusted requests to temporary file
			File tempFile = new File("src//source//temp.csv");
			FileWriter writer = new FileWriter(tempFile);
			for (int i = 0; i < rows.size(); i++) {
				String[] row = rows.get(i);
				line = String.join(",", row);
				writer.write(line);
				if (i < rows.size() - 1) {
					writer.write("\n");
				}
			}
			writer.close();

			// mv temporary file to requestsFile, overwriting it.
			java.nio.file.Files.move(java.nio.file.Paths.get(tempFile.getAbsolutePath()),
					java.nio.file.Paths.get(floorRequests.getAbsolutePath()),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
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
	public Message createRequest(Scanner reader) {
		String data = reader.nextLine();
		String[] values = data.split(", ");
		Message req = null;

		if (validateRequest(values)) {
			req = new Message(values[0], Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]),
					Integer.parseInt(values[4]));
			setDelay(values[0]);
		}
		return req;

	}

	/**
	 * Method to read the time of request from the file, and set the message delay
	 * value such that the request will execute at the desired time
	 * 
	 * @param timeString : String value read from first column of the file.
	 */
	private void setDelay(String timeString) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(timeString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		// Change the date from Jan 1st 1970 to today, to accurately retrieve Millis for
		// current time
		Calendar today = Calendar.getInstance();
		calendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

		// determine the difference in Millis from current time to calculated time of
		// request
		long differenceMillis = calendar.getTimeInMillis() - System.currentTimeMillis();

		// set messageDelay to difference (time till request comes in)
		messageDelay = (differenceMillis > 0 ? differenceMillis : 0);
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
			if (request[0].matches("\\d+:\\d+:\\d+") && request[1].matches("\\d+") && request[2].equals("UP")
					|| request[2].equals("DOWN") && request[3].matches("\\d+") && request[4].matches("\\d+")) {
				return true;
			}
		}
		System.err.println("An incorrect request was recieved");
		return false;
	}

	/**
	 * Send and gets messages through UDP
	 * 
	 * @param sendingMessage, either null or a Message Object, it is a message
	 *                        object when there is a message to be sent, null when
	 *                        you want to receive a message
	 * @return Message or null, if you passed null, it will return a message.
	 *         Otherwise it will return null.
	 */

	private void sendAndGetMessage(Message sendingMessage, boolean send) {
		DatagramPacket sending; // both packets
		DatagramPacket receiving;
		try {
			if (send) {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); // set up byte array streams to turn
																				// Message into a byte array
				ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);

				objectStream.writeObject(sendingMessage);
				objectStream.flush();

				byte sendingData[] = byteStream.toByteArray();
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(),
						FloorHandler.FLOOR_HANDLER_PORT); // Send to floor handler

				sendAndReceive.send(sending);

				byte receivingData[] = new byte[1];
				receiving = new DatagramPacket(receivingData, 1); // Get response back, should be only length 1
				sendAndReceive.receive(receiving);

				byteStream.close();
				objectStream.close();
			} else {
				byte sendingData[] = new byte[1];
				sending = new DatagramPacket(sendingData, sendingData.length, InetAddress.getLocalHost(),
						FloorHandler.FLOOR_HANDLER_PORT); // send void message to notify that I want a message
				sendAndReceive.send(sending);

				byte receivingData[] = new byte[FloorHandler.MAX_DATA_SIZE];
				receiving = new DatagramPacket(receivingData, receivingData.length); // get message from handler
				sendAndReceive.receive(receiving);

				// unpack into an object
				ByteArrayInputStream byteStream = new ByteArrayInputStream(receiving.getData());

				ObjectInputStream objectStream = new ObjectInputStream(byteStream);

				sendingMessage = (Message) objectStream.readObject();
				latestReturned = sendingMessage;
				Thread.sleep(100);

				byteStream.close();
				objectStream.close();
			}

		} catch (IOException | ClassNotFoundException | InterruptedException e) {
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
		// create thread to read requests, and store in shared data at appropriate time
		Thread requestHandlerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					scanner = new Scanner(floorRequests);
					scannerActive = true;
					while (scannerActive) {
						if (scanner.hasNextLine()) {
							requestsHandled ++;
							Message req = createRequest(scanner);
							if(req != null) {
								// sleep until the specified time is reached
								Thread.sleep(messageDelay); 
								messageDelay = 0; // reset delay 
								requestQueue.add(req); // add to shared data 		
							}
						} else {
							requestsComplete = true; // no more requests
							scanner.close();
							scannerActive = false;
							break;
						}
					}
				} catch (FileNotFoundException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		requestHandlerThread.start();
		// send and receive thread 
		while (true) {
			// if there are no requests, send empty message to handler
			if (requestQueue.isEmpty()) {
				sendAndGetMessage(null, true);
			} else if (requestsComplete && requestQueue.isEmpty() ) {
				// notify scheduler that there are no more requests to be sent
				Message done = new Message("done");
				sendAndGetMessage(done, true);
			} else {
				Calendar calendar = Calendar.getInstance();
				// pop request
				Message req = requestQueue.remove(0);
				System.out.println("Passing message: " + req + " at " + calendar.getTime());
				sendAndGetMessage(req, true);
			}
			
			Message returned = new Message("");
			sendAndGetMessage(returned, false);
		}
	}
	
	/**
	 * Method used to close sockets for testing purposes
	 */
	public void closeSockets() {
		sendAndReceive.close();
	}
	
	/**
	 * Method used to manually close the socket, for testing 
	 */
	public void closeScanner() {
		scanner.close();
		scannerActive = false;
	}
	
	public Message getLatestReturned() {
		return latestReturned;
	}
	
	public int getRequestsHandled() {
		return requestsHandled;
	}

	public static void main(String[] args) {
		File file = new File("src//source//Requests.csv");
		Thread f = new Thread(new Floor(file), "Floor");
		f.start();
	}
}