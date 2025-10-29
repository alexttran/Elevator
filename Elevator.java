import java.util.*;

public class Elevator implements Runnable {
  private final int id;
  private int currentFloor;
  private Direction currentDirection;
  private final Set<Integer> destinationFloors;
  private final List<Passenger> passengers;
  private final int capacity;
  private volatile boolean running;
  
  public enum Direction {
    UP, DOWN, IDLE
  }
  
  public Elevator(int id) {
    this.id = id;
    this.currentFloor = 1; // Start at ground floor
    this.currentDirection = Direction.IDLE;
    this.destinationFloors = new TreeSet<>();
    this.passengers = new ArrayList<>();
    this.capacity = 8;
    this.running = true;
  }
  
  public synchronized int getCurrentFloor() {
    return currentFloor;
  }
  
  public synchronized Direction getCurrentDirection() {
    return currentDirection;
  }
  
  public synchronized int getPassengerCount() {
    return passengers.size();
  }
  
  public synchronized boolean isFull() {
    return passengers.size() >= capacity;
  }
  
  public synchronized boolean canPickup(int floor, Direction direction) {
    if (isFull()) {
      return false;
    }
    if (currentDirection == Direction.IDLE) {
      return false;
    }
    // Check if elevator is going in the same direction and pickup is on the way
    if (currentDirection == direction) {
      if (destinationFloors.isEmpty()) {
        return false;
      }
      if (direction == Direction.UP) {
        // Find the furthest destination floor going up
        int maxDestination = currentFloor;
        for (int dest : destinationFloors) {
          if (dest > maxDestination) {
            maxDestination = dest;
          }
        }
        // Only pickup if the floor is between current and max destination
        return floor >= currentFloor && floor <= maxDestination;
      } else if (direction == Direction.DOWN) {
        // Find the lowest destination floor going down
        int minDestination = currentFloor;
        for (int dest : destinationFloors) {
          if (dest < minDestination) {
            minDestination = dest;
          }
        }
        // Only pickup if the floor is between min destination and current
        return floor <= currentFloor && floor >= minDestination;
      }
    }
    return false;
  }
  
  public synchronized void addDestination(int floor) {
    destinationFloors.add(floor);
  }
  
  public synchronized void addPassenger(Passenger passenger) {
    if (passengers.size() < capacity) {
      passengers.add(passenger);
      destinationFloors.add(passenger.getDestinationFloor());
    }
  }
  
  public synchronized void removePassenger(Passenger passenger) {
    passengers.remove(passenger);
  }
  
  @Override
  public void run() {
    while (running) {
      try {
        boolean shouldMove = false;
        synchronized (this) {
          if (destinationFloors.isEmpty()) {
            currentDirection = Direction.IDLE;
          } else {
            // Determine direction based on next destination
            updateDirection();
            shouldMove = true;
          }
        }
        if (!shouldMove) {
          // Sleep outside synchronized block when idle
          Thread.sleep(100);
          continue;
        }
        // Move one floor and take 1 second to travel
        boolean stoppedAtFloor = false;
        synchronized (this) {
          if (currentDirection == Direction.UP) {
            currentFloor++;
          } else if (currentDirection == Direction.DOWN) {
            currentFloor--;
          }
          System.out.println("Elevator " + id + " is at floor " + currentFloor + " going " + currentDirection);
          // Check if we've reached a destination
          if (destinationFloors.contains(currentFloor)) {
            destinationFloors.remove(currentFloor);
            stoppedAtFloor = true;
            // Drop off passengers
            List<Passenger> toRemove = new ArrayList<>();
            for (Passenger p : passengers) {
              if (p.getDestinationFloor() == currentFloor) {
                toRemove.add(p);
                p.arrive();
              }
            }
            passengers.removeAll(toRemove);
            // Notify waiting passengers at this floor
            notifyAll();
          }
        }
        // If stopped at a floor, pause for 1 second for boarding/alighting
        if (stoppedAtFloor) {
          System.out.println("Elevator " + id + " doors open at floor " + currentFloor);
          Thread.sleep(1000);
          System.out.println("Elevator " + id + " doors closed at floor " + currentFloor);
        }
        // Take 1 second to move between floors
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
  
  private void updateDirection() {
    if (destinationFloors.isEmpty()) {
      currentDirection = Direction.IDLE;
      return;
    }
    
    int nextFloor = getNextDestination();
    
    if (nextFloor > currentFloor) {
      currentDirection = Direction.UP;
    } else if (nextFloor < currentFloor) {
      currentDirection = Direction.DOWN;
    } else {
      currentDirection = Direction.IDLE;
    }
  }
  
  private int getNextDestination() {
    if (destinationFloors.isEmpty()) {
      return currentFloor;
    }
    
    // Get the next floor in the current direction
    if (currentDirection == Direction.UP || currentDirection == Direction.IDLE) {
      for (int floor : destinationFloors) {
        if (floor >= currentFloor) {
          return floor;
        }
      }
    }
    
    if (currentDirection == Direction.DOWN || currentDirection == Direction.IDLE) {
      int maxFloor = -1;
      for (int floor : destinationFloors) {
        if (floor <= currentFloor && floor > maxFloor) {
          maxFloor = floor;
        }
      }
      if (maxFloor != -1) {
        return maxFloor;
      }
    }
    
    // If no floors in current direction, get any floor
    return destinationFloors.iterator().next();
  }
  
  public int getId() {
    return id;
  }
  
  public void stop() {
    running = false;
  }
}
