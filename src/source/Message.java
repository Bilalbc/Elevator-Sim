package source;

import java.io.Serializable;

/**
 * The message class stores information about requests inside to be sent between
 * the floor and elevator. Implements Serializable.
 * 
 * @author Kousha Motazedian
 * @version 4.0
 * @date March 25th, 2023
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 6878979521805641286L;

	private String time;
	private String direction;
	private int startFloor;
	private int destinationFloor;
	private String returnMessage;

	/**
	 * Message Constructor
	 * 
	 * @param time,             String: Time of the request
	 * @param startFloor,       int: Which floor the request was made
	 * @param direction,        String: UP or DOWN
	 * @param destinationFloor, int: The destination floor of the request
	 */
	public Message(String time, int startFloor, String direction, int destinationFloor) {
		this.time = time;
		this.direction = direction;
		this.startFloor = startFloor;
		this.destinationFloor = destinationFloor;
	}

	/**
	 * Message Constructor
	 * 
	 * @param reply, String: Reply message
	 */
	public Message(String reply) {
		this.returnMessage = reply;
	}

	/**
	 * Getter for the request time
	 * 
	 * @return time, String
	 */

	public String getTime() {
		return this.time;
	}

	/**
	 * Getter for the direction of the request
	 * 
	 * @return direction, String
	 */

	public String getDirection() {
		return this.direction;
	}

	/**
	 * Getter for the start floor of the request
	 * 
	 * @return startFloor, int
	 */

	public int startFloor() {
		return this.startFloor;
	}

	/**
	 * Getter for the destination floor of the request
	 * 
	 * @return destination Floor, int
	 */
	public int destinationFloor() {
		return this.destinationFloor;
	}

	/**
	 * Sets the return message (reply) from the elevator.
	 * 
	 * @param message, String
	 */
	public void setReturnMessage(String message) {
		this.returnMessage = message;
	}

	/**
	 * Getter of the return message (reply).
	 * 
	 * @return returnMessage, String
	 */
	public String getReturnMessage() {
		return this.returnMessage;
	}

	@Override
	/**
	 * toString override for the object.
	 */
	public String toString() {
		return this.direction + " " + this.time + " Start Floor: " + this.startFloor + " Destination Floor : "
				+ this.destinationFloor;
	}

	@Override
	/**
	 * equals override for the object
	 */
	public boolean equals(Object msg) {

		if (!(msg instanceof Message)) {
			return false;
		}

		Message dupe = (Message) msg;

		return (((this.startFloor == dupe.startFloor) && (this.destinationFloor == dupe.destinationFloor)
				&& (this.direction.equals(dupe.direction)) && (this.time.equals(dupe.time)))
				|| (this.returnMessage.equals(dupe.returnMessage)));
	}

}