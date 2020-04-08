===================================
=== SYSC3303B PROJECT - GROUP 8 ===
===================================

The report can be found in the root directory, titled report.pdf.

The demo video can be found in the root directory, titled demo.mp4. Alternatively, it can be found here:
https://drive.google.com/file/d/1mdJhyR-6_HkjIXeYxq6VqEWorGypLce7/view

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
- Tests (see IMPORTANT NOTE below): In Eclipse, right click on the /src/test/java folder on the Package Explorer pane and click Run As > JUnit Test.
  - IMPORTANT NOTE: Each test (found in the src/test/java/ folder) must be run individually (right click on EACH FUNCTION in the test file in Eclipse and click Run As > JUnit Test).

=============
=== FILES ===
=============
- src/main/java/elevator/
    - ArrivalSensor.java
    - Elevator.java
    - ElevatorAction.java
    - ElevatorFault.java
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
    - ElevatorHardFaultTest.java
    - ElevatorSoftFaultTest.java
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
- src/main/resources/data.txt
  - This contains the test data (a sample floor input file) used by the floor subsystem and floor reader.
- src/test/resources/
    - data.txt
    - doorStuckClosedFaultData.txt
    - doorStuckOpenFaultData.txt
    - elevatorStuckFaultData.txt
    - elevatorSystemTestData.txt
    - sensorFaultData.txt
- uml/ClassDiagram.pdf

======================
=== WORK BREAKDOWN ===
======================

=======================
=== FINAL ITERATION ===
=======================
Code:
    - Scheduler Fault Handling: Randy
      - Elevator Fault Handling Counterpart: Layne
    - Scheduler Algorithm: Galen, Layne and Tan
Testing: Layne, Randy, and Galen
Diagrams:
    - Class Diagrams: Mike and Randy
    - State Machine Diagram: Randy
    - Sequence Diagrams: Layne

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
    - Class Diagram: Mike and Randy
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
