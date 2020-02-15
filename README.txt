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
In Eclipse, right click on the /src/test/java folder on the Package Explorer pane and click Run As > JUnit Test.

Each of the subsystem tests (FloorSubsystemTest, ElevatorSubsystemTest, SchedulerTest) showcase the entire system in action.

=============
=== FILES ===
=============
- src/main/java/core/
    - Main.java
- src/main/java/elevator/
    - Elevator.java
    - ElevatorAction.java
    - ElevatorData.java
    - ElevatorEvent.java
    - ElevatorResponse.java
    - ElevatorState.java
    - ElevatorSubsystem.java
- src/main/java/floor/
    - FloorData.java
      - Represents one entry in the floor subsystem input file as an object.
    - FloorReader.java
      - Handles reading the floor subsystem input files.
    - FloorSubsystem.java
- src/main/java/scheduler/
    - Scheduler.java
- src/test/java/elevator/
    - ElevatorDataTest.java
    - ElevatorSubsystemTest.java
- src/test/java/floor/
    - FloorDataTest.java
    - FloorReaderTest.java
    - FloorSubsystemTest.java
- src/test/java/scheduler/
    - SchedulerTest.java
- src/test/resources/floorData.txt
  - This contains the test data (a sample floor input file) used by the floor subsystem and floor reader.
- uml/ClassDiagram.pdf
- uml/SequenceDiagram.pdf
- uml/StateMachineDiagram.pdf

======================
=== WORK BREAKDOWN ===
======================
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
    - Sequence Diagram: Tan and Galen
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
