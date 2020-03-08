===================================
=== SYSC3303B PROJECT - GROUP 8 ===
===================================

=========================
=== TABLE OF CONTENTS ===
=========================
- Setup
- Running
- Files
- Work Breakdown

=============
=== SETUP ===
=============
1. In Eclipse, File > Import... > Maven > Existing Maven Projects > Select ElevatorControlSystem as Root Directory > Finish

Important notes:
- This project uses Java 10 (the default on lab computers).
- If for some reason JUnit is not imported correctly, please ensure it is on your classpath. Eclipse can do this for you (i.e. highlight over the error and click Eclipse's suggestion of adding JUnit to the classpath).
- If the project does not import correctly for some reason, try importing it like this:
  - File > Import... > General > Projects from Folder or Archive > Select ElevatorControlSystem as Import source > Finish

===============
=== RUNNING ===
===============
- To run, launch the following programs in order:
    1. Scheduler.java (src/main/scheduler/Scheduler.java)
    2. ElevatorSystem.java (src/main/elevator/ElevatorSystem.java)
    3. FloorSystem.java (src/main/floor/FloorSystem.java)
    This will run through the data specified in data.txt.
- Tests: In Eclipse, right click on the /src/test/java folder on the Package Explorer pane and click Run As > JUnit Test.
  - Note: Each test must be run individually (right click on the test file in Eclipse and click Run As > JUnit Test).

=============
=== FILES ===
=============
- src/main/java/core/
    - Main.java
- src/main/java/elevator/
    - ArrivalSensor.java
    - Elevator.java
    - ElevatorAction.java
    - ElevatorResponse.java
    - ElevatorState.java
    - ElevatorSystem.java
    - ElevatorSubsystem.java
- src/main/java/floor/
    - FloorData.java
      - Represents one entry in the floor subsystem input file as an object.
    - FloorReader.java
      - Handles reading the floor subsystem input files.
    - Floor.java
    - FloorSystem.java
- src/main/java/scheduler/
    - ElevatorStatus.java
    - Scheduler.java
- src/main/java/global/
    - Globals.java
- src/test/java/classes/
    - RunnableElevatorSystem.java
    - RunnableFloorSystem.java
    - RunnableScheduler.java
- src/test/java/elevator/
    - ArrivalSensorTest.java
    - ElevatorTest.java
    - ElevatorSystem.java
    - ElevatorSubsystemTest.java
- src/test/java/floor/
    - FloorDataTest.java
    - FloorReaderTest.java
    - FloorSystemTest.java
- src/test/java/scheduler/
    - BestElevatorTest.java
    - SchedulerTest.java
- src/main/resources/data.txt and src/test/resources/data.txt
  - This contains the test data (a sample floor input file) used by the floor subsystem and floor reader.
- src/main/resources/floorSystemTestData.txt and src/test/resources/floorSystemTestData.txt
- src/main/resources/elevatorSystemTestData.txt and src/test/resources/elevatorSystemTestData.txt
- uml/ClassDiagram.pdf
- uml/SequenceDiagram.pdf

======================
=== WORK BREAKDOWN ===
======================
===================
=== ITERATION 3 ===
===================
Code:
    - Scheduler Networking: Randy
    - ElevatorSystem Networking: Layne
    - FloorSystem Networking: Layne and Randy
    - Elevator Movement and ArrivalSensor: Layne
      - Scheduler Counterpart: Randy
    - Scheduler Algorithm: Tan and Galen
    - Testing: Layne, Randy, and Galen
Diagrams:
    - Class Diagram: Mike
    - Sequence Diagram: Layne

===================
=== ITERATION 2 ===
===================
Code:
    - Scheduler (state, scheduling elevator): Randy
    - Floor (scheduling elevator): Randy
    - Elevator (moving): Layne
    - ElevatorSubsystem (state): Layne
Diagrams:
    - Class Diagram: Tan, Galen, Randy, Layne and Mike
    - Sequence Diagram: Tan, Galen, and Layne
    - State Machine Diagram: Tan, Galen, and Randy

===================
=== ITERATION 1 ===
===================
Code:
    - Scheduler: Randy
    - Elevator: Mike and Layne
    - Floor: Galen
    - FloorReader: Tan and Randy
Testing: Everyone
UML: Everyone
