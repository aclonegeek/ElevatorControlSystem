===================================
=== SYSC3303B PROJECT - GROUP 8 ===
===================================

=============
=== SETUP ===
=============
1. Clone the repo: git clone https://github.com/aclonegeek/ElevatorControlSystem.git
2. In Eclipse, File > Import... > Maven > Existing Maven Projects > Select ElevatorControlSystem as Root Directory > Finish

===============
=== RUNNING ===
===============
In Eclipse, right click on the /src/test/java folder on the Package Explorer pane and click Run As > JUnit Test.

Each of the subsystem tests (FloorSubsystemTest, ElevatorSubsystemTest, SchedulerTest) showcase the entire system in action.

=============
=== FILES ===
=============
- src/main/java/core/
    - ElevatorSubsystem.java
    - FloorData.java
      - Represents one entry in the floor subsystem input file as an object.
    - FloorReader.java
      - Handles reading the floor subsystem input files.
    - FloorSubsystem.java
    - Scheduler.java
- src/test/java/core/
    - FloorDataTest.java
    - FloorReaderTest.java
    - FloorSubsystemTest.java
    - ElevatorSubsystemTest.java
    - SchedulerTest.java
- src/test/resources/floorData.txt
  - This contains the test data (a sample floor input file) used by the floor subsystem and floor reader.
- uml/ClassDiagram.png

======================
=== WORK BREAKDOWN ===
======================
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
