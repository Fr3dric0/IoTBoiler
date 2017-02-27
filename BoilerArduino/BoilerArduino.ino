#include <SoftwareSerial.h>
#include <PLabBTSerial.h>
#include <Servo.h>

#define   TX_PIN    2
#define   RX_PIN    3

#define   SERVO_PIN 11
#define   HUMIN_PIN A0


/**
 * =======================
 *         SERVO
 * =======================
 */
Servo servo;

/**
 * =======================
 *     HUMIDITY SENSOR
 * =======================
 */
const int defaultPos = 10;
int prevBoilVal = 0;
bool boiling = false;

/**
 * =======================
 *    BLUETOOTH SENSOR
 * =======================
 * 
 * COMMANDS
 * 
 * H - Hello ping, used to say we are ready to communicate
 * B - Bye. Tell to stop (Might not be necessary
 * R - Run, start boiling (should send back 0 or 1)
 * I - Is running, used to check if water is boiling (0 or 1)
 * 
 */
PLabBTSerial btSerial(TX_PIN, RX_PIN);
char commands[] = {'R', 'I', 'H'};

void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);

  servo.attach(SERVO_PIN);
  servo.write(defaultPos); // Set to start
}

void loop() {
  char cmd = getCommand();
  // INIT BOIL
  if (cmd == commands[0]) {
    // if it is still boiling
    if (!boiling) {
      boil();
    } else {
      sendMsg("1");
    }
  
  // CHECK IF BOILING
  } else if (cmd == commands[1]) {
    sendMsg(boiling ? "2" : "3"); // 2 = boils, 3 = not boiling
    
  // PING (response = "4" (good), "5" (other))
  } else if (cmd == commands[2]) {
    sendMsg("4");
  // UNKNOWN COMMAND
  } else {
    sendMsg("\0");
  }

  if (boiling) {
    boiling = isBoiling(getHumin());
  }
}


void boil() {
   servo.write(50);

   delay(500);
   servo.write(defaultPos);

   boiling = true;
   prevBoilVal = getHumin(); // Set start state of humidity
   sendMsg("0");
}

void sendMsg(String msg) {
  btSerial.print(msg);
}

char getCommand() {
  // Block further steps untill conenction is available
  //Serial.println(btSerial.available() ? "BT-CONN" : "BT-NO-CONN");
  while (!btSerial.available()) { 
    
    //Serial.println(boiling ? "BOILING" : "NOT BOILING");
    if (boiling) {
      boiling = isBoiling(getHumin());
    }
    delay(200);
  }
  
  if (boiling) {
    boiling = isBoiling(getHumin());
  }
  return btSerial.read();
}

boolean isBoiling(int current) {
  if (current < prevBoilVal) {
    sendMsg("3");
    return false;
  }
  return true;
}

int getHumin() {
  int val = analogRead(HUMIN_PIN);
  Serial.print("HUM = ");
  Serial.println(val);

  // TODO - Calibrate values
  if (val > 1100) {
    val = 1100;
  } else if (val > 800) {
    val = 800;
  } else if (val > 400){
    val = 400;
  } else {
    val = 0;
  }

  return val;
}

