IoT Water Boiler
================

Project to modify our water boiler to become an IoT water boiler, with the help of Arduino and Java.

## Arduino
The arduino part is built up of an Arduino Uno, breadboard, Bluetooth sensor, servo, and humidity sensor.
Arduino recieves commands through the bluetooth chip. 

When the command `boil` is recieved, arduino will move the servo, switching the button on the waterboiler.
We will then use the humidity sensor to detect when the boiler is finished (_when the humidity stops growing_).

> NOTICE: if the humidity sensor is to unreliable, we can instead use an average time.
> Plan ahead. Use the boiling time to calculate the amount of water boiled

### Limitations
Because of the limited reliability of the bluetooth chip, we should limit us to only send and recieve single characters

## Java Server
The coordination central is the Java Spark-server, which communicates with the clients (_HTTP request_) and arduino (_Bluetooth signals_). 

### Available Requests

1. `GET /boil` - Initiates boiling
2. `GET /isboiling` - Checks if water is boiling
3. `GET /lastboil` - Gets last boiling datetime
4. `GET /stats` - Returns statistics of boiling. Like last boiling datetime, and amount of water boiled

## JavaFX client
The clientside part of the project is a JavaFX program, which has the ability to initiate boiling, present statistics etc.

## Summed up
If all components communicate corretly, we should be left with a fully operational **IoT Water Boiler**.
