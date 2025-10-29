import java.util.concurrent.CountDownLatch;

public class Passenger {
    private final int startFloor;
    private final int destinationFloor;
    private final Elevator.Direction direction;
    private final CountDownLatch arrivalLatch;
    private final long requestTime;
    
    public Passenger(int startFloor, int destinationFloor) {
        this.startFloor = startFloor;
        this.destinationFloor = destinationFloor;
        this.direction = destinationFloor > startFloor ? Elevator.Direction.UP : Elevator.Direction.DOWN;
        this.arrivalLatch = new CountDownLatch(1);
        this.requestTime = System.nanoTime();
    }
    
    public int getStartFloor() {
        return startFloor;
    }
    
    public int getDestinationFloor() {
        return destinationFloor;
    }
    
    public Elevator.Direction getDirection() {
        return direction;
    }
    
}