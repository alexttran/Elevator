import javax.swing.*;
import java.awt.*;

public class ElevatorGUI extends JFrame {
    private static final int NUM_FLOORS = 10;
    private static final int NUM_USERS = 5;
    private final ElevatorController controller;
    private final UserPanel[] userPanels;
    private final ElevatorStatusPanel statusPanel;

    public ElevatorGUI() {
        this.controller = new ElevatorController(3);
        this.userPanels = new UserPanel[NUM_USERS];
        this.statusPanel = new ElevatorStatusPanel();

        setupUI();
        startStatusUpdater();
    }

    private void setupUI() {
        setTitle("Elevator System - 10 Floors, 3 Elevators");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create main panel with user panels
        JPanel mainPanel = new JPanel(new GridLayout(1, NUM_USERS, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < NUM_USERS; i++) {
            userPanels[i] = new UserPanel(i + 1);
            mainPanel.add(userPanels[i]);
        }

        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        setSize(1400, 700);
        setLocationRelativeTo(null);
    }

    private void startStatusUpdater() {
        Timer timer = new Timer(500, e -> statusPanel.updateStatus());
        timer.start();
    }

    class UserPanel extends JPanel {
        private final int userId;
        private final JTextField currentFloorField;
        private final JComboBox<String> directionBox;
        private final JTextField destinationFloorField;
        private final JButton callButton;
        private final JTextArea statusArea;
        private boolean inProgress = false;

        public UserPanel(int userId) {
            this.userId = userId;
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createTitledBorder("User " + userId));

            // Input panel
            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

            inputPanel.add(new JLabel("Current Floor:"));
            currentFloorField = new JTextField("1");
            inputPanel.add(currentFloorField);

            inputPanel.add(new JLabel("Direction:"));
            directionBox = new JComboBox<>(new String[]{"UP", "DOWN"});
            inputPanel.add(directionBox);

            inputPanel.add(new JLabel("Destination:"));
            destinationFloorField = new JTextField();
            inputPanel.add(destinationFloorField);

            callButton = new JButton("Call Elevator");
            callButton.addActionListener(e -> handleElevatorCall());
            inputPanel.add(new JLabel());
            inputPanel.add(callButton);

            add(inputPanel, BorderLayout.NORTH);

            // Status area
            statusArea = new JTextArea(20, 20);
            statusArea.setEditable(false);
            statusArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            JScrollPane scrollPane = new JScrollPane(statusArea);
            add(scrollPane, BorderLayout.CENTER);

            addStatus("Ready to call elevator");
        }

        private void handleElevatorCall() {
            if (inProgress) {
                addStatus("Already in progress!");
                return;
            }

            try {
                int currentFloor = Integer.parseInt(currentFloorField.getText().trim());
                String directionStr = (String) directionBox.getSelectedItem();
                int destinationFloor = Integer.parseInt(destinationFloorField.getText().trim());

                // Validate inputs
                if (currentFloor < 1 || currentFloor > NUM_FLOORS) {
                    addStatus("ERROR: Current floor must be 1-" + NUM_FLOORS);
                    return;
                }

                if (destinationFloor < 1 || destinationFloor > NUM_FLOORS) {
                    addStatus("ERROR: Destination must be 1-" + NUM_FLOORS);
                    return;
                }

                Elevator.Direction direction = directionStr.equals("UP") ?
                    Elevator.Direction.UP : Elevator.Direction.DOWN;

                // Validate direction matches destination
                if (direction == Elevator.Direction.UP && destinationFloor <= currentFloor) {
                    addStatus("ERROR: UP requires destination > current floor");
                    return;
                }

                if (direction == Elevator.Direction.DOWN && destinationFloor >= currentFloor) {
                    addStatus("ERROR: DOWN requires destination < current floor");
                    return;
                }

                // Start elevator journey in a separate thread
                new Thread(() -> makeJourney(currentFloor, direction, destinationFloor)).start();

            } catch (NumberFormatException ex) {
                addStatus("ERROR: Please enter valid numbers");
            }
        }

        private void makeJourney(int currentFloor, Elevator.Direction direction, int destinationFloor) {
            try {
                inProgress = true;
                SwingUtilities.invokeLater(() -> callButton.setEnabled(false));

                addStatus("Calling elevator from floor " + currentFloor + "...");
                controller.requestPickup(currentFloor, direction);

                addStatus("Waiting for elevator...");
                Elevator elevator = controller.waitForElevator(currentFloor, direction);

                addStatus("Elevator " + elevator.getId() + " arrived!");

                // Board elevator
                Passenger passenger = new Passenger(currentFloor, destinationFloor);
                synchronized (elevator) {
                    elevator.addPassenger(passenger);
                }

                addStatus("Boarded Elevator " + elevator.getId() +
                         ", going to floor " + destinationFloor + "...");

                // Wait for arrival
                passenger.waitForArrival();

                addStatus("Arrived at floor " + destinationFloor + "!");
                addStatus("Journey complete! Ready for next trip.");

            } catch (InterruptedException ex) {
                addStatus("ERROR: Journey interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                addStatus("ERROR: " + ex.getMessage());
            } finally {
                inProgress = false;
                SwingUtilities.invokeLater(() -> callButton.setEnabled(true));
            }
        }

        private void addStatus(String message) {
            SwingUtilities.invokeLater(() -> {
                statusArea.append("[User " + userId + "] " + message + "\n");
                statusArea.setCaretPosition(statusArea.getDocument().getLength());
            });
        }
    }

    class ElevatorStatusPanel extends JPanel {
        private final JLabel[] elevatorLabels;

        public ElevatorStatusPanel() {
            setLayout(new GridLayout(1, 3, 10, 10));
            setBorder(BorderFactory.createTitledBorder("Elevator Status"));
            setPreferredSize(new Dimension(0, 80));

            elevatorLabels = new JLabel[3];
            for (int i = 0; i < 3; i++) {
                elevatorLabels[i] = new JLabel("", SwingConstants.CENTER);
                elevatorLabels[i].setFont(new Font("Monospaced", Font.BOLD, 14));
                elevatorLabels[i].setBorder(BorderFactory.createEtchedBorder());
                add(elevatorLabels[i]);
            }
        }

        public void updateStatus() {
            for (int i = 0; i < controller.getElevators().size(); i++) {
                Elevator elevator = controller.getElevators().get(i);
                int floor = elevator.getCurrentFloor();
                Elevator.Direction dir = elevator.getCurrentDirection();
                int passengers = elevator.getPassengerCount();

                String arrow = "";
                if (dir == Elevator.Direction.UP) arrow = "↑";
                else if (dir == Elevator.Direction.DOWN) arrow = "↓";
                else arrow = "•";

                String text = String.format(
                    "<html><center>Elevator %d<br>Floor: %d %s<br>Passengers: %d/8</center></html>",
                    elevator.getId(), floor, arrow, passengers
                );

                elevatorLabels[i].setText(text);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ElevatorGUI gui = new ElevatorGUI();
            gui.setVisible(true);
        });
    }
}

