# SYSC 3303 Elevator Simulation Group Project Iteration 2

## Code Overview

#### Source Package:

**Message.java ->** A class that creates a message object that is used to pass around information between Floor.java, Scheduler.java, and Elevator.java.

**Floor.java ->** A class that creates the floor subsystem in the simulation. It takes information from Requests.csv and creates Message objects from said information. The messages objects are then sent to a scheduler in which the floor subsystem waits for a response to read.

**Scheduler.java ->** A class that creates the scheduler that is used in the simulation. The scheduler will receive messages from the floor class and will sort the incoming request floors by number. When a message is received, it will be added to the queue. It then adds the destination to the queue of a specific elevator thread. The scheduler also manages whether it would be efficient for a specific elevator (based on its number and current state) to take a certain request.

**Elevator.java ->** A class that receives floor requests from the scheduler which indicates the order in which the elevator should pick up/drop off riders.
The elevator has states for when the doors have opened or closed, when the elevator is moving up or down, and when the elevator has made a stop.

**Main.java ->** Includes the main method used to start the simulation.


#### Test Package:

**Iteration2Test.java ->** A testing class that is used to test the functionality of the simulation, more specifically testing the eleavtor states, events for Elevator and Scheduler, and the passing of messages between the Floor, Scheduler and Elevator. 

**badTestData.csv ->** Data used for handling failures in the system.

**testData.csv ->** Data used for testing the functionality of the Floor, Scheduler, and Elevator.

## How to run
To run the code, make sure that all files are present and are in the right spots. The code overview above tells you which classes should be where. (Requests.csv) should be in the source package. Run Main.java to start the program, as it contains the main method.

## Contributions
Everyone contributed to the design and planning of the program.

**Sequence Diagram:** Mohamed Kaddour
**Class Diagram:** Bilal Chaudhry
**State Diagram** Kousha Motazedian 
**JUnit Testing:** Bilal Chaudhry
**Message.java:** Kousha Motazedian
**Scheduler.java:** Mohamed Kaddour, Akshay Vashisht
**Floor.java:** Kousha Motazedian, Matthew Parker
**Elevator.java:** Kousha Motazedian, Matthew Parker, Akshay Vashisht
