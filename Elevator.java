public class Elevator {
    private int currentFloor;
    private final int minFloor;
    private final int maxFloor;

    public Elevator(int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.currentFloor = minFloor; // Start at the minimum floor
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
