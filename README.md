# SYSC 3303 Elevator Simulation Group Project Iteration 1

## Code Overview

#### Source Package:

**Message.java ->** A class that creates a message object that is used to pass around information between Floor.java, Scheduler.java, and Elevator.java.

**Floor.java ->** A class that creates the floor subsystem in the simulation. It takes information from Requests.csv and creates Message objects from said information. The messages objects are then sent to a scheduler in which the floor subsystem waits for a response to read.

**Scheduler.java ->** A class that creates the scheduler that is used in the simulation. The scheduler takes the message from the floor and it sends allows the elevator to take the request when the elevator is idle. The scheduler gets the reply from the elevator and gives it to the floor to read.

**Elevator.java ->** A class that creates the elevator subsystem that is used for the simulation. The elevators gets requests from the floor via the scheduler and reads them. Once the request has been read, the elevator then sends a reply back to the floor subsystem through the scheduler.

**Main.java ->** Includes the main method used to start the simulation.


#### Test Package:

**Iteration1Test.java ->** A testing class that is used to test the functionality of the simulation, more specifically testing the message sharing between the subsystem.

## How to run
To run the code, make sure that all files are present and are in the right spots. The code overview above tells you which classes should be where. (Requests.csv) should be in the source package. Run Main.java to start the program, as it contains the main method.

## Contributions
Everyone contributed to the design and planning of the program.

**Sequence Diagram:** Mohamed Kaddour
**Scheduler.java:** Mohamed Kaddour
**Class Diagram:** Bilal Chaudhry
**JUnit Testing:** Bilal Chaudhry
**Message.java:** Kousha Motazedian
**Floor.java:** Kousha Motazedian, Matthew Parker
**Elevator.java:** Akshay Vashisht
