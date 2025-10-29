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

}
