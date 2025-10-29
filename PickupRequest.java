public class PickupRequest implements Comparable<PickupRequest> {
  private final int floor;
  private final Elevator.Direction direction;
  private final long timestamp;
  private final double tiebreaker; // Random value for breaking ties
  
  public PickupRequest(int floor, Elevator.Direction direction) {
    this.floor = floor;
    this.direction = direction;
    this.timestamp = System.nanoTime();
    this.tiebreaker = Math.random();
  }
  
  public int getFloor() {
    return floor;
  }
  
  public Elevator.Direction getDirection() {
    return direction;
  }
  
  public long getTimestamp() {
    return timestamp;
  }
  
  @Override
  public int compareTo(PickupRequest other) {
    // First compare by timestamp
    int timeCompare = Long.compare(this.timestamp, other.timestamp);
    if (timeCompare != 0) {
        return timeCompare;
    }
    // If timestamps are equal, use tiebreaker
    return Double.compare(this.tiebreaker, other.tiebreaker);
  }
}
