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
import java.time.LocalTime;
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
	private ArrayList<Message> requestQueue;
	private boolean requestsComplete;

	private Scanner reader;

	/**
	 * Constructor for Floor class
	 * 
	 * @param sch, Scheduler being used
	 */
	public Floor(File file) {
		this.floorRequests = file;
		this.requestQueue = new ArrayList<Message>();
		this.requestsComplete = false;
		try {
			this.sendAndReceive = new DatagramSocket();
//			sendAndReceive.setSoTimeout(10000);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		updateTimes();
	}

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
				int rand = (int) (Math.random() * 10) + 3;
				cal.add(Calendar.SECOND, rand);
				row[0] = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
						+ cal.get(Calendar.SECOND);
			}

			reader.close();
			// Write out the modified CSV file
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
	private Message createRequest(Scanner reader) {
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

		Calendar today = Calendar.getInstance();
		calendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

		long differenceMillis = calendar.getTimeInMillis() - System.currentTimeMillis();

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
				System.out.println(sendingMessage.getReturnMessage());

				byteStream.close();
				objectStream.close();
			}

		} catch (IOException | ClassNotFoundException e) {
			reader.close();
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
		Thread requestHandlerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					reader = new Scanner(floorRequests);
					while (true) {
						if (reader.hasNextLine()) {
							Message req = createRequest(reader);
							Thread.sleep(messageDelay);
							messageDelay = 0;
							requestQueue.add(req);
						} else {
							requestsComplete = true;
						}
					}
				} catch (FileNotFoundException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		requestHandlerThread.start();
		while (true) {
			if (requestQueue.isEmpty()) {
				sendAndGetMessage(null, true);
				System.out.println(messageDelay);
			} else {
				Calendar calendar = Calendar.getInstance();

				Message req = requestQueue.remove(0);
				System.out.println("Passing message: " + req + " at " + calendar.getTime());
				sendAndGetMessage(req, true);
			}
			if (requestsComplete) {
				// notify scheduler that there are no more requests to be sent
				Message done = new Message("done");
				sendAndGetMessage(done, true);
			}
			Message returned = new Message("");
			sendAndGetMessage(returned, false);
		}
	}

	public static void main(String[] args) {
		File file = new File("src//source//Requests.csv");
		Thread f = new Thread(new Floor(file), "Floor");
		f.start();
	}
}