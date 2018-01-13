//Global variables:
/*--reserved pins that are not declared here--*/
//13, 11 and 12 are also reserved for the radio
//Used Pins: 2, 3, 4, 5, 6, ( (7,8 )Radio), 9, ( ( 11, 12, 13) radio)

/*-----( Import needed libraries )-----*/
#include <SPI.h>   // Comes with Arduino IDE
#include "RF24.h"  // Download and Install (See above)
#include <Wire.h>  // Comes with Arduino IDE
#include <MAX17043.h>
#include <RTClibExtended.h>
#include <LowPower.h>

RTC_DS3231 RTC;      //we are using the DS3231 RTC

MAX17043 batteryMonitor(10);
/*-----( Declare objects )-----*/
/* Hardware configuration: Set up nRF24L01 radio on SPI bus plus (usually) pins 7 & 8 (Can be changed) */
RF24 radio(7, 8);

//pins buttons
#define ldrPin				A0 // pin for the Light dependant resistance. ok
#define powerOnLed			A1 //pin for the rain sensor
#define powerToWater        4 //Give power to the mofset to turn on/off the water
#define waterPin1			5 // pin for turning on the solenoid (Water) ok
#define waterPin2			6 // pin for turning on the solenoid (Water) 
#define waterOnLed			3 // pin for turning on the solenoid (Water) 

//buttons to turn water manually.
#define btnTurnWaterOnPin  	9 //btn to turn on the water manually

/*-----------Communication format constants-------------------------*/
const char workerId[4] = "002";
//start and end of the code
const char START        	= 'z';//start of command
const char END          	= 'e'; //end of command
//send or recive code
const char SENDING_CODE     	= 's';
const char REVC_CONFIRM_CODE  	= 'c';  //confirmation from the worker.
//command codes
const char OPEN_WATER       = 'o';  //command to send to open the valve
const char CLOSE_WATER      = 'a';  //command to send to close the valve
const char GET_SENSOR_DATA  = 'g';  //command to send to get the status
const char SAVE_POWER       = 'p';
//from worker
const char SELF_IDENTIFY      = 'i';  //code that the worker is self identifying to the controller.
const char WATER_MANUAL_OVR	  = 'm'; 	//code that the worker send that tells the controller that the water has been turned on manually
const char WATER_TURNED_OFF		= 'w'; //code that the worker send that tells the controller that the water has been turned Off.
const char RAIN_NO_ON      = 'r';//code that the worker send that tells the controller that no watering is done since it's raining
const char BATT_ALERT      = 'b';//code that the worker send that tells the controller that the battery is low
const char WOKE_UP         = 'd';//code that the worker send that tells the controller that the worker woke up

//with controller values
char command[20];
boolean waterRunning 	= false;  //if it's watering

//common values
uint8_t ldrValue			= 0;
uint8_t rainValue 			= 2;
uint8_t battPercentInt 		= 0;
int waterOnInMins 		= 0; // number of minutes that the water must be on

//timers
unsigned long waterInterval 	    = 0; // number of milliseconds that the water must be on
unsigned long prevWaterMillis 		= 0;
unsigned long batteryWarningMillis 	= 0;
//end timers

boolean battAlertOn = false; //boolean for battery alert.

char buffer[3]; // for conversion
String bufferString; //conversion buffer from int to char

uint8_t nbrOfRetry    = 60; //number of retry before stopping to find controller.

/*-----( Declare Radio Variables )-----*/
byte addresses[][6] = {"mst01", "wk002"}; // These will be the names of the "Pipes"

//date
int year			= 0;
uint8_t month			= 0;
uint8_t day				= 0;
uint8_t hour			= 0;
uint8_t minute			= 0;
uint8_t seconds			= 0;

boolean start = true;
boolean dateRecvOk = false;

void wakeUp()        // here the interrupt is handled after wakeup
{
}
void setup() {

  bufferString.reserve(10);

  pinMode(waterPin1, OUTPUT); //set pin as output for watering
  pinMode(waterPin2, OUTPUT); //set pin as output for watering
  pinMode(powerToWater, OUTPUT); //set pin as output for controlling the mosfet

  pinMode(powerOnLed, OUTPUT); //set pin as output for power
  pinMode(waterOnLed, OUTPUT); //set pin as output for watering

  digitalWrite(powerToWater, HIGH);

  pinMode(btnTurnWaterOnPin, INPUT); //Button 1

  radio.begin();          // Initialize the nRF24L01 Radio
  radio.setChannel(108);  // 2.508 Ghz - Above most Wifi Channels
  radio.setDataRate(RF24_250KBPS); // Fast enough.. Better range
  // radio.setPALevel(RF24_PA_LOW);
  //radio.setPALevel(RF24_PA_HIGH);

  radio.setPALevel(RF24_PA_MAX);
  radio.setRetries(15, 15);
  radio.setCRCLength(RF24_CRC_16);
  radio.openWritingPipe(addresses[0]);
  radio.openReadingPipe(1, addresses[1]);
  //radio.startListening();

  batteryMonitor.begin();  // Resets MAX17043

  RTC.begin();

  //Set SQW pin to OFF (in my case it was set by default to 1Hz)
  //The output of the DS3231 INT pin is connected to this pin
  //It must be connected to arduino D2 pin for wake-up
  RTC.writeSqwPinMode(DS3231_OFF);
  RTC.armAlarm(1, false);
  RTC.clearAlarm(1);
  RTC.alarmInterrupt(1, false);

  delay(300);
}

void loop() {

  if (start) {
    starting();
    start = false;
  }
  if (radio.available()) {

    readCommand();//read commmand

    if (command[1] == OPEN_WATER) {

      //set number of minutes it should be on
      buffer[0] = command[6];
      buffer[1] = command[7];
      buffer[2] = command[8];

      waterOnInMins = atoi(buffer);
      waterOnInMins += 10;//add 10 minutes , if no command received from the controller shut it down. ( safety measure)

      //turn on valve, not manually.
      turnValOnOff(true, false);
      if (rainValue > 1) {
        sendMessageToController(OPEN_WATER, REVC_CONFIRM_CODE);
      } else {
        sendMessageToController(RAIN_NO_ON, REVC_CONFIRM_CODE);
      }
    }
    else if (command[1] == CLOSE_WATER) {
      //turn on valve
      turnValOnOff(false, false);
      sendMessageToController(CLOSE_WATER, REVC_CONFIRM_CODE);
    }
    else if (command[1] == GET_SENSOR_DATA) {
      //send data info to controller.
      sendMessageToController(GET_SENSOR_DATA, REVC_CONFIRM_CODE);
    }
    else if (command[1] == SAVE_POWER) {
      buffer[0] = command[6];
      buffer[1] = command[7];
      buffer[2] = '\0';
      hour = atoi(buffer);
      buffer[0] = command[8];
      buffer[1] = command[9];
      minute = atoi(buffer);

      //send message as received
      sendMessageToController(SAVE_POWER, REVC_CONFIRM_CODE);
      delay(400); //for the message to transmit before powering down
      powerDown();
      //after wake up, send an other message
      sendMessageToController(WOKE_UP, SENDING_CODE);
    }
  }
  processButtons(); //process the buttons that turn on/off the water manually

  //**********************//
  //automatically turn valve off --> if from controller, 10 min has been added.
  if (waterRunning && (millis() - prevWaterMillis) > waterInterval) {
    turnValOnOff(false, true);
  }

  //if battery alert is on, send message to controller to alert.20 min
  if (batteryMonitor.isAlerting() && (millis() - batteryWarningMillis) > 1200000) {
    batteryWarningMillis = millis();
    sendMessageToController(BATT_ALERT, SENDING_CODE); //send alert to pi
  }

}
void turnValOnOff(boolean turnOn, boolean manual) {
  if (turnOn) {
    //  getRainstatus();
    if ( rainValue > 1) { //not raining too hard, water
      waterOn();
      digitalWrite(waterOnLed, HIGH); //turn on the LED for water

      waterRunning = true;
      waterInterval = (waterOnInMins * 60000);  //set in millis the time the vals should be open
      prevWaterMillis = millis(); //get the time we turned the water on for the auto shut off to be correct.
      if (manual) { //send message to the controller that the water  was turned On manually
        sendMessageToController(WATER_MANUAL_OVR, SENDING_CODE);
      }
    } else {
      if (manual) { //send message to the controller that it won't turn on since it's raining
        sendMessageToController(RAIN_NO_ON, SENDING_CODE);
      }
    }
  } else if (!turnOn) {
    waterOff();

    digitalWrite(waterOnLed, LOW); //LED turn off

    waterOnInMins = 0;
    waterInterval = 0;
    waterRunning = false;

    if (manual) { //send message to the controller that the water  was turned off manually
      sendMessageToController(WATER_TURNED_OFF, SENDING_CODE);
    }
  }
}
void getLDRstatus()
{ //Va0 = 5 * R1/(R1+R2) --> high resistance when dark --> 800- 1024: lots of light. 0 (5v) no light. 100k resistor
  //R1 = 10k, R2 = 5k => Va0 = 5 * 10000/(10000 + 5000) = 5 * 10/15 = 3.33V
  //map(long x, long in_min, long in_max, long out_min, long out_max)
  //map return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  ////0 = dark, 1=low light, 2=medium light, 3=full light
  ldrValue = map(analogRead(ldrPin), 0, 1023, 0, 10);
}
//void getRainstatus()
//{
// rainValue = map(analogRead(rainSensorPin), 0, 1024, 0, 3);
//}

//read command from wireless
void readCommand() {

  radio.read( &command, sizeof(char) * 20 );

  if (start) { //when self identifying, reset the clock
    //zic000yymmddhhmmss
    buffer[0] = command[6];
    buffer[1] = command[7];
    buffer[2] = '\0';
    year			= atoi(buffer);

    buffer[0] = command[8];
    buffer[1] = command[9];
    month			= atoi(buffer);
    buffer[0] = command[10];
    buffer[1] = command[11];
    day			= atoi(buffer);
    buffer[0] = command[12];
    buffer[1] = command[13];
    hour			= atoi(buffer);
    buffer[0] = command[14];
    buffer[1] = command[15];
    minute			= atoi(buffer);
    buffer[0] = command[16];
    buffer[1] = command[17];
    seconds		= atoi(buffer);

    year += 2000; //add 2000 to year to make it to 2017

    RTC.adjust(DateTime(year, month, day, hour, minute, seconds));

    dateRecvOk = true;
  }

  if (command[0] != ' ' && command[2] == SENDING_CODE &&
      command[3] == workerId[0] && command[4] == workerId[1] &&
      command[5] == workerId[2]) {

  } else {
    command[0] = '\0'; command[1] = '\0';
  }
}
void sendMessageToController(char cmd, char confOrSend) {
  
  getLDRstatus();

  // getRainstatus();
  getBatteryStatus();


  command[0] = START;
  command[1] = cmd;
  command[2] = confOrSend;
  command[3] = workerId[0];
  command[4] = workerId[1];
  command[5] = workerId[2];
  //LDR
  bufferString = String(ldrValue);
  bufferString.toCharArray(buffer, 2);
  command[6] = buffer[0];
  //RAIN
  bufferString = String(3);
  bufferString.toCharArray(buffer, 2);
  command[7] = buffer[0];
  if (waterRunning) {
    command[8] = '31'; //ascii code for 1
  } else {
    command[8] = '30';//ascii code for 0
  }
  //Battery
  bufferString = String(battPercentInt);
  bufferString.toCharArray(buffer, 3);
  if (battPercentInt < 10) { //1 char less
    command[9] = '30';//ascii code for 0
    command[10] = buffer[0];
    command[11] = END;
  } else {
    command[9] = buffer[0];
    command[10] = buffer[1];
    command[11] = END;
    command[12] = '\0';
  }


  radio.stopListening();

  radio.write( &command, sizeof(command));            // Send data, checking for error ("!" means NOT)

  radio.startListening();
}
void starting() { //when 1st starting , detect the controller

  unsigned long started_waiting_at;

  uint8_t i = 0; //counter for number of retry.

  while (true) {
    digitalWrite(powerOnLed, HIGH); //high to start
    digitalWrite(waterOnLed, HIGH); //high to start
    delay(1000);
    
    //sending identification to controller
    sendMessageToController(SELF_IDENTIFY, SENDING_CODE);
    digitalWrite(waterOnLed, LOW); //high to start

    delay(5);
    started_waiting_at = micros();
    boolean timeout = false;
    while ( !radio.available() ) {                            // While nothing is received
      if (micros() - started_waiting_at > 3000000 ) {        // If waited longer than 3 seconds, indicate timeout and exit while loop
        timeout = true;
        break;
      }
    }

    digitalWrite(waterOnLed, HIGH); //high to start
    delay(1000);

    digitalWrite(waterOnLed, LOW);

    if (!timeout) { //confirmation received, continue
      readCommand(); //just read the confirmation
      if (dateRecvOk) { //if everything is ok , blink fast.
        for (int b = 0; b < 5; b++) {
          digitalWrite(powerOnLed, HIGH); //green light on
          delay(500);
          digitalWrite(powerOnLed, LOW);
          delay(500);
        }
        digitalWrite(powerOnLed, HIGH);
        break;
      } else {
        for (int b = 0; b < 5; b++) {
          digitalWrite(waterOnLed, HIGH); //Blue light on - Error
          delay(500);
          digitalWrite(waterOnLed, LOW);
          delay(500);
        }
      }
    } else if (timeout && i > nbrOfRetry) {
      for (int i = 0; i <= 10; i++) { //if nbr of retry exceeded, blink both water and power on led.
        digitalWrite(powerOnLed, HIGH);
        digitalWrite(waterOnLed, HIGH);
        delay(500);
        digitalWrite(powerOnLed, LOW);
        digitalWrite(waterOnLed, LOW);
        delay(500);
      }
      radio.stopListening();
      radio.powerDown();
      digitalWrite(powerOnLed, LOW);
      break;
    } else if (timeout) {
      delay(1000);
      i = i + 1;
    }
  }
}

//***********************
//buttons on the left for manual override of the water.
void processButtons() {
  int btnWtrOnStatus = digitalRead(btnTurnWaterOnPin); //top button (on / off)

  if (btnWtrOnStatus == HIGH ) {
    if (!waterRunning ) {
      waterOnInMins = 999;//turn on for a long time
      turnValOnOff(true, true);
    } else if (waterRunning) {
      turnValOnOff(false, true);
    }
    delay(500); //to read the button only once
  }
}
void   getBatteryStatus() {
  battPercentInt = (int)batteryMonitor.getBatteryPercentage();

  if (battPercentInt > 99) {
    battPercentInt = 99;
  }
}
void waterOn() {
  digitalWrite(waterPin1, HIGH); //pins that control power direction
  digitalWrite(waterPin2, LOW); //pins that control power direction

  digitalWrite(powerToWater, LOW); //turn on Mofset
  delay(100);
  digitalWrite(powerToWater, HIGH); //turn OFF Mofset
  
}
void waterOff() {

  digitalWrite(waterPin1, LOW); //pins that control power direction
  digitalWrite(waterPin2, HIGH); //pins that control power direction

  digitalWrite(powerToWater, LOW); //turn on Mofset
  delay(100);
  digitalWrite(powerToWater, HIGH); //turn OFF Mofset
}

void powerDown() {

  //clear any pending alarms
  RTC.armAlarm(2, false);
  RTC.clearAlarm(2);
  RTC.alarmInterrupt(2, false);
  RTC.setAlarm(ALM2_MATCH_HOURS, minute, hour, 0);   //set your wake-up time here
  RTC.alarmInterrupt(2, true);

  radio.stopListening(); //radio
  radio.powerDown();

  batteryMonitor.sleep();
  //battery
  delay(100);

  attachInterrupt(0, wakeUp, LOW);

  LowPower.powerDown(SLEEP_FOREVER, ADC_OFF, BOD_OFF);

  detachInterrupt(0);
  radio.powerUp();
  radio.startListening();

  RTC.armAlarm(2, false);
  RTC.clearAlarm(2);
  RTC.alarmInterrupt(2, false);

  batteryMonitor.wake();
}

void blueLedBlink(){

        digitalWrite(waterOnLed, LOW);
        delay(600);
        digitalWrite(waterOnLed, HIGH);
        delay(100);
 
}

