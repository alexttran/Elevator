import java.util.NoSuchElementException;
import java.util.Scanner;

public class ElevatorSystem {
  private static final int NUM_FLOORS = 10;
  private static final int NUM_ELEVATORS = 3;
  private final ElevatorController controller;

  public ElevatorSystem() {
      this.controller = new ElevatorController(NUM_ELEVATORS);
  }

  public void start() {
    System.out.println("=== Elevator System Started ===");
    System.out.println("Building has " + NUM_FLOORS + " floors");
    System.out.println(NUM_ELEVATORS + " elevators available");
    System.out.println();

    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println("\n=== New Passenger ===");
      System.out.println("Press ENTER to call an elevator (or type 'exit' to quit):");
      String input = scanner.nextLine();

      if (input.equalsIgnoreCase("exit")) {
        break;
      }

      // Handle this passenger sequentially
      handlePassenger(scanner);
    }
    controller.shutdown();
    scanner.close();
    System.out.println("Elevator system shut down.");
  }

  private void handlePassenger(Scanner scanner) {
    try {            // Get current floor
      int currentFloor;
      while (true) {
          System.out.print("What floor are you on? (1-" + NUM_FLOORS + "): ");
          try {
            currentFloor = Integer.parseInt(scanner.nextLine().trim());
            if (currentFloor >= 1 && currentFloor <= NUM_FLOORS) {
                  break;
              } else {
                  System.out.println("Invalid floor. Please enter a number between 1 and " + NUM_FLOORS);
              }
          } catch (NumberFormatException e) {
              System.out.println("Invalid input. Please enter a number.");
          }
      }

      // Get desired direction
      Elevator.Direction direction;
      while (true) {
          System.out.print("Which direction? (UP/DOWN): ");
          String dirInput = scanner.nextLine().trim().toUpperCase();

          if (dirInput.equals("UP")) {
              if (currentFloor == NUM_FLOORS) {
                  System.out.println("You're on the top floor. You can only go DOWN.");
                  continue;
              }
              direction = Elevator.Direction.UP;
              break;
          } else if (dirInput.equals("DOWN")) {
              if (currentFloor == 1) {
                  System.out.println("You're on the ground floor. You can only go UP.");
                  continue;
              }
              direction = Elevator.Direction.DOWN;
              break;
          } else {
              System.out.println("Invalid direction. Please enter UP or DOWN.");
          }
      }

      // Request pickup
      System.out.println("Calling elevator...");
      controller.requestPickup(currentFloor, direction);

      // Wait for elevator to arrive
      System.out.println("WAIT...");
      Elevator elevator = controller.waitForElevator(currentFloor, direction);

      System.out.println("Elevator " + elevator.getId() + " has arrived at floor " + currentFloor + "!");

      // Get destination floor
      int destinationFloor;
      while (true) {
        if (direction == Elevator.Direction.UP) {
          System.out.print("Which floor would you like to go to? (" + (currentFloor + 1) + "-" + NUM_FLOORS + "): ");
        } else {
          System.out.print("Which floor would you like to go to? (1-" + (currentFloor - 1) + "): ");
        }

        try {
          destinationFloor = Integer.parseInt(scanner.nextLine().trim());

          // Validate destination based on direction
          if (direction == Elevator.Direction.UP) {
            if (destinationFloor > currentFloor && destinationFloor <= NUM_FLOORS) {
              break;
            } else {
              System.out.println("Invalid floor. Must be above floor " + currentFloor);
            }
          } else {
            if (destinationFloor < currentFloor && destinationFloor >= 1) {
              break;
            } else {
              System.out.println("Invalid floor. Must be below floor " + currentFloor);
            }
          }
        } catch (NumberFormatException e) {
          System.out.println("Invalid input. Please enter a number.");
        }
      }

      // Create passenger and board elevator
      Passenger passenger = new Passenger(currentFloor, destinationFloor);
      synchronized (elevator) {
        elevator.addPassenger(passenger);
      }

      System.out.println("You have boarded Elevator " + elevator.getId() + ". Going to floor " + destinationFloor + "...");

      // Wait until passenger arrives at destination
      passenger.waitForArrival();

      System.out.println("You have arrived at floor " + destinationFloor + "! Have a nice day!");

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.println("Journey interrupted.");
      } catch (NoSuchElementException e) {
        System.out.println("Input error: Scanner closed or no input available.");
      } catch (Exception e) {
        System.out.println("An error occurred: " + e.getClass().getName() + " - " + e.getMessage());
        e.printStackTrace();
      }
    }

  public static void main(String[] args) {
        ElevatorSystem system = new ElevatorSystem();
        system.start();
    }
}
