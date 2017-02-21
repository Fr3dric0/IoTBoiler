#include <SoftwareSerial.h>
#include <PLabBTSerial.h>

#define   TX_PIN    2
#define   RX_PIN    3

PLabBTSerial btSerial(TX_PIN, RX_PIN);
/** COMMANDS
 *  H - Hello ping, used to say we are ready to communicate
 *  B - Bye. Tell to stop (Might not be necessary
 *  R - Run, start boiling (should send back 0 or 1)
 *  I - Is running, used to check if water is boiling (0 or 1)
 */
char commands[] = {'R', 'I', 'H', 'Q'};
void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);
}

void loop() {
  bool newData = false;
  char cmd = getCommand();
  Serial.println(cmd);

  if (cmd == commands[0]) {
    sendMsg("0"); // 0 viss start, 1 viss feil
  } else if (cmd == commands[1]) {
    sendMsg("2"); // 2 viss koker, 3 viss ikkje
  } else if (cmd == commands[2]) {
    sendMsg("H"); // H == Hei
  } else if (cmd == commands[3]) {
    sendMsg("B"); // B == Bye
  } else {
    sendMsg("\0");
  }
}


void sendMsg(String msg) {
  btSerial.print(msg);
}

char getCommand() {
  // Block further steps untill conenction is available
  while (!btSerial.available()) { }
  return btSerial.read();
}

