===================================
=== SYSC3303B PROJECT - GROUP 8 ===
===================================

=========================
=== TABLE OF CONTENTS ===
=========================
- Setup
- Running
- Files

=============
=== SETUP ===
=============
1. In Eclipse, File > Import... > Maven > Existing Maven Projects > Select ElevatorControlSystem as Root Directory > Finish

Important notes:
- This project uses Java 10.
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
