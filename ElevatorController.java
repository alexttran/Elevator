import java.util.*;
import java.util.concurrent.*;
public class ElevatorController {
  private final List<Elevator> elevators;
  private final PriorityQueue<PickupRequest> pickupQueue;
  private final ExecutorService executorService;
  private volatile boolean running;
  
  public ElevatorController(int numElevators) {
      this.elevators = new ArrayList<>();
      this.pickupQueue = new PriorityQueue<>();
      this.executorService = Executors.newFixedThreadPool(numElevators + 1); // +1 for dispatcher
      this.running = true;
      
      // Create and start elevators
      for (int i = 0; i < numElevators; i++) {
          Elevator elevator = new Elevator(i + 1);
          elevators.add(elevator);
          executorService.submit(elevator);
      }
      
      // Start dispatcher thread
      executorService.submit(this::dispatchRequests);
  }
  
  public synchronized void requestPickup(int floor, Elevator.Direction direction) {
      PickupRequest request = new PickupRequest(floor, direction);
      pickupQueue.offer(request);
      notifyAll();
  }
  
  private void dispatchRequests() {
    while (running) {
      try {
        PickupRequest request;
        
        synchronized (this) {
          while (pickupQueue.isEmpty() && running) {
              wait(100);
          }
          
          if (!running) break;
          
          request = pickupQueue.peek();
          if (request == null) continue;
        }
        
        // Find best elevator for this request
        Elevator bestElevator = findBestElevator(request);
        
        if (bestElevator != null) {
          synchronized (this) {
            pickupQueue.poll(); // Remove the request
          }
          
          synchronized (bestElevator) {
            bestElevator.addDestination(request.getFloor());
          }
          
          System.out.println("Dispatched Elevator " + bestElevator.getId() + 
                           " to floor " + request.getFloor() + " (direction: " + request.getDirection() + ")");
        }
        
        Thread.sleep(100); // Check every 100ms
        
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
  private Elevator findBestElevator(PickupRequest request) {
    Elevator bestElevator = null;
    int minDistance = Integer.MAX_VALUE;
    
    // If no elevator is going in the same direction, find the closest idle elevator
    if (bestElevator == null) {
      for (Elevator elevator : elevators) {
        if (elevator.getCurrentDirection() == Elevator.Direction.IDLE && !elevator.isFull()) {
          int distance = Math.abs(elevator.getCurrentFloor() - request.getFloor());
          if (distance < minDistance) {
            minDistance = distance;
            bestElevator = elevator;
          }
        }
      }
    }
    
    return bestElevator;
  }
  
  public Elevator waitForElevator(int floor, Elevator.Direction direction) throws InterruptedException {
    while (true) {
      for (Elevator elevator : elevators) {
        synchronized (elevator) {
          if (elevator.getCurrentFloor() == floor && !elevator.isFull()) {
            // Check if the elevator is going in the right direction or is idle
            Elevator.Direction elevatorDir = elevator.getCurrentDirection();
            if (elevatorDir == direction || elevatorDir == Elevator.Direction.IDLE) {
              return elevator;
            }
          }
        }
      }
      Thread.sleep(100); // Check every 100ms
    }
  }
  
  public void shutdown() {
    running = false;
    for (Elevator elevator : elevators) {
      elevator.stop();
    }
    executorService.shutdownNow();
  }
  
  public List<Elevator> getElevators() {
    return elevators;
  }
}