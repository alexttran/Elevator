# Elevator

Code that simulates an elevator.
Made for a Bluestaq take home challenge

Details:
- 3 Elevators
- 10 Floors
- Max capacity: 8 people/elevator
- 1 sec travel time per floor
- From a floor, the user sends a request for going either up or down
- If there are no requests for an elevator, the elevators stay idle at their last stop

Pickup Logic:
- First, if any elevators are currently in motion and pass through the user floor in the user’s desired direction, that elevator picks them up\
- If none fit that criteria, the nearest idle elevator picks them up\
- If all elevators are busy, the user is queued and the next open elevator will be assigned\
- Additional 1 sec to pickup and drop off passengers.
