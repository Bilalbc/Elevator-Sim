package source;

/**
 * @author Akshay V., Kousha Motazedian, Matthew Parker
 * @version 2.0
 * @date February 27th, 2023
 *
 *       An Elevator class that receives floor requests from the scheduler which
 *       indicates the order in which the elevator should pick up/drop off
 *       riders. As of Iteration2, the elevator follows the states of being able
 *       to move up, move down, stop, close doors and open doors.
 */

public class Elevator implements Runnable {

	private int currentFloor;
	private int assignedNum;
	private Scheduler scheduler;
	private int destination = 0; // current destination floor, 0 means no destination at the moment

	public static enum ElevatorStates {
		DOORSOPEN, DOORSCLOSED, MOVINGUP, MOVINGDOWN, STOPPED
	};

	private ElevatorStates currentState;

	/**
	 * Constructor for Elevator
	 * 
	 * @param sch, Scheduler being used
	 */
	public Elevator(Scheduler sch) {
		this.scheduler = sch;
		this.currentFloor = 1; // elevator starts at floor 1
		this.assignedNum = 1; // only have 1 elevator right now
		this.currentState = ElevatorStates.DOORSCLOSED; // Elevator starts at a closed state
	}

	@Override
	/**
	 * Run method of the elevator. Gets the floor request from the scheduler for
	 * this specific elevator. The elevator is initially in the stopped state and
	 * will query its floor queue through a call to the Scheduler in order to
	 * determine when it should stop to drop off/pick up people.
	 */
	public void run() {
		while (!scheduler.isClosed()) {
			try {
				if (destination == currentFloor && currentState != ElevatorStates.DOORSCLOSED) {
					currentState = ElevatorStates.STOPPED;
					Thread.sleep(1000);
					// If there is a destination request for the current floor, the doors open
					currentState = ElevatorStates.DOORSOPEN;

					System.out.println(
							"Elevator " + assignedNum + ": Letting People in and out on floor " + currentFloor + "!");
					Thread.sleep(1000);
					this.currentState = ElevatorStates.DOORSCLOSED; // Set to doors closed
				}

				// Lets the scheduler know which floor it is on and its state
				scheduler.passState(currentFloor, currentState, assignedNum);
				Thread.sleep(1000);
				// If the elevator has reached its destination or does not have one (0)
				if (currentFloor == destination || destination == 0) {
					int newDestination = scheduler.readMessage();

					// If the new destination is not 0 (meaning no destinations left), the elevator
					// takes on the new destination
					if (newDestination != 0) {
						destination = newDestination;
					}

					// If the destination still has not changed (because readMessage returned 0),
					// floor is not sending anymore requests, and the scheduler is holding no more
					// requests for the elevator, close the system.
					if (currentFloor == destination && scheduler.isRequestsComplete()
							&& scheduler.getElevatorQueueSize() == 0) {

						// Pass state to ensure floor is not waiting forever
						scheduler.passState(currentFloor, currentState, assignedNum);
						scheduler.setClosed();
					}
				}

				if (destination != 0) {
					if (destination > currentFloor) { // check if the elevator needs to move up or down
						currentState = ElevatorStates.MOVINGUP;
					} else if (destination < currentFloor) {
						currentState = ElevatorStates.MOVINGDOWN;
					}

					Thread.sleep(1000);
					if (currentState.equals(ElevatorStates.MOVINGUP)) {
						currentFloor++;
					}
					if (currentState.equals(ElevatorStates.MOVINGDOWN)) {
						currentFloor--;
					}

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Getter method to access the currentFloor of the Elevator
	 * 
	 * @return int : current floor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * Getter method to access the assignedNum of the elevator
	 * 
	 * @return int : assigned Number
	 */
	public int getAssignedNum() {
		return assignedNum;
	}

	/**
	 * Getter method for the current State of the elevator
	 * 
	 * @return ElevatorStates : the current state of the elevator
	 */
	public ElevatorStates getcurrentState() {
		return currentState;
	}

}
