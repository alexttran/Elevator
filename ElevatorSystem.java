public class ElevatorSystem {
  private static final int NUM_FLOORS = 10;
  private static final int NUM_ELEVATORS = 3;
  private final ElevatorController controller;

  public ElevatorSystem() {
      this.controller = new ElevatorController(NUM_ELEVATORS);
  }

  public void start() {

  }

  public static void main(String[] args) {
        ElevatorSystem system = new ElevatorSystem();
        system.start();
    }
}
