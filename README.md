IoT Water Boiler
================

Project to modify a water boiler to become an Internet of Things Water Boiler, using Arduino and Java Spark.

### Features

1. Initiate boiling through HTTP calls
2. Notification and statistics of when the boiling started and ended
3. Automatically calculates the amount of water boiled

## Arduino
The arduino part is built up of an Arduino Uno, breadboard, Bluetooth sensor, servo, and humidity sensor.
Arduino recieves commands through the bluetooth chip. 

When the command `boil` is recieved, arduino will move the servo, switching the button on the waterboiler.
We will then use the humidity sensor to detect when the boiler is finished (_when the humidity stops growing_).

> NOTICE: if the humidity sensor is to unreliable, we can instead use an average time.
> Plan ahead. Use the boiling time to calculate the amount of water boiled

### Limitations
Because of the limited reliability of the bluetooth chip, the communication between Arduino and Java Spark is restricted to one character to and from. Therefore, Java Spark is responsible for building most of the logic and statistics.

## Java Server
The coordination central is the Java Spark-server, which communicates with the clients (_HTTP_) and Arduino (_Bluetooth_). 

### Available Requests

1. `GET /boil` - Initiates boiling
2. `GET /isboiling` - Checks if water is boiling
3. `GET /lastboil` - Gets last boiling datetime
4. `GET /stats` - Returns statistics of boiling. Like last boiling datetime, and amount of water boiled

## JavaFX client
The clientside part of the project is a JavaFX program, which has the ability to initiate boiling, present statistics etc.

> It is much better to implement your own client for initiating and recieving signals.
