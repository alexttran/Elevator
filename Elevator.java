import java.util.*;

public class Elevator {

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

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void goToFloor(int floor) {
        if (floor < minFloor || floor > maxFloor) {
            throw new IllegalArgumentException("Requested floor is out of bounds.");
        }
        currentFloor = floor;
        System.out.println("Elevator moved to floor: " + currentFloor);
    }

    public void goUp() {
        if (currentFloor < maxFloor) {
            currentFloor++;
            System.out.println("Elevator moved up to floor: " + currentFloor);
        } else {
            System.out.println("Elevator is already at the top floor.");
        }
    }

    public void goDown() {
        if (currentFloor > minFloor) {
            currentFloor--;
            System.out.println("Elevator moved down to floor: " + currentFloor);
        } else {
            System.out.println("Elevator is already at the bottom floor.");
        }
    }
}
