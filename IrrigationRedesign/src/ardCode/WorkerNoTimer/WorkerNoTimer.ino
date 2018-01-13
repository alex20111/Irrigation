//Global variables:
/*--reserved pins that are not declared here--*/
//Pin a4 and a5 (analogue) are reserved for the LCD
//13, 11 and 12 are also reserved for the radio

/*-----( Import needed libraries )-----*/
#include <SPI.h>   // Comes with Arduino IDE
#include "RF24.h"  // Download and Install (See above)
#include <Wire.h>  // Comes with Arduino IDE
#include <LiquidCrystal_I2C.h>
#include <LowPower.h>
#include <RTClibExtended.h> 

RTC_DS3231 RTC;      //we are using the DS3231 RTC  

MAX17043 batteryMonitor(10);
/*-----( Declare objects )-----*/
/* Hardware configuration: Set up nRF24L01 radio on SPI bus plus (usually) pins 7 & 8 (Can be changed) */
RF24 radio(7, 8);

/*----LCD-------  0x27 or 0x3f*/
LiquidCrystal_I2C lcd(0x3f, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);  // Set the LCD I2C address

//pins buttons
#define ldrPin				A0 // pin for the Light dependant resistance. ok
#define rainSensorPin		A1 //pin for the rain sensor
#define waterOnPin			4 // pin for turning on the solenoid (Water) ok
#define waterOFFPin			6 // pin for turning on the solenoid (Water) 

//buttons to turn water manually.
#define btnTurnWaterOnPin  	9 //btn to turn on the water manually
#define btnManualSavePin   	5 //btn to save the settings for btnTurnWaterOnPin

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
const char WOKE_UP         = 'd';//code that the worker send that tells the controller that the worker woke up

//with controller values
char command[20];
boolean waterRunning 	= false;  //if it's watering

//common values
boolean turnOff = false; // boolean to decide if we turn off the water manually when it's running
uint8_t ldrValue			= 0;  //Test
uint8_t rainValue 			= 2;
boolean addTime 		= false; //Init of the add time of the manual turn on water.
int waterOnInMins 		= 0; // number of minutes that the water must be on
int waterOnTimer		= 0; //timer that will display how many minutes are remaining to water on the LCD.
//timers
unsigned long prevWaterOnTimer  = 0; //to display watering countdown.. always needs to start a 0 so it will display the number of minutes right away
unsigned long waterInterval 	   = 0; // number of milliseconds that the water must be on
unsigned long prevSleepLCDMillis = 0; // number of milliseconds that the LCD is on (5 min)
unsigned long prevWaterMillis = 0;
//end timers
unsigned int  lcdInterval 		= 30000; // number of milliseconds that the LCD is on (30sec)
boolean lcdOn 			= true; //tell if when pressing any buttons if we should turn the lcd on.
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
  start = true;
  pinMode(waterOnPin, OUTPUT); //set pin as output for watering ON
  pinMode(waterOFFPin, OUTPUT); //set pin as output for watering OFF

  digitalWrite(waterOnPin, HIGH);
  
  pinMode(btnTurnWaterOnPin, INPUT);
  pinMode(btnManualSavePin, INPUT);

  lcd.begin(16, 2);
  turnLcdOn(); //turn the LCD on

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
  radio.startListening();


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

	  turnLcdOn();
      if (command[1] == OPEN_WATER) {
        prevWaterOnTimer = 0; //set the tiemr to 0 in case it was not reset.
        //set number of minutes it should be on
        buffer[0] = command[6];
        buffer[1] = command[7];
        buffer[2] = command[8];

        waterOnInMins = atoi(buffer);		
        waterOnInMins += 10;//add 10 minutes , if no command received from the controller shut it down. ( safety measure)

        //turn on valve
        turnValOnOff(true, false);
        if (rainValue > 1) {
          sendMessageToController(OPEN_WATER, REVC_CONFIRM_CODE);
          displayWateringMessage();
        } else {
          sendMessageToController(RAIN_NO_ON, REVC_CONFIRM_CODE);
        }				
      }
      else if (command[1] == CLOSE_WATER) {
        //turn on valve
        turnValOnOff(false, false);
        sendMessageToController(CLOSE_WATER, REVC_CONFIRM_CODE);
        lcd.clear(); lcd.setCursor(0, 0);
        lcd.print(F("Water off."));
        lcd.setCursor(0, 1); lcd.print(F("Waiting for cmd"));
      }
      else if (command[1] == GET_SENSOR_DATA) {
        //send data info to controller.
        sendMessageToController(GET_SENSOR_DATA, REVC_CONFIRM_CODE);
      }
     else if (command[1] == SAVE_POWER){
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
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Auto turn off"));
    delay(800);
    turnValOnOff(false, true);
    //message to lcd 
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Waiting for"));
    lcd.setCursor(0, 1); lcd.print(F("Command")); 
  }

  //automatically turn lcd off
  if (lcdOn && (millis() - prevSleepLCDMillis) > lcdInterval) {
    lcd.noBacklight();
    //turn lcd off
    lcdOn = false;
	  //display generic message when lcd is off. Except when it's watering
    waterOnInMins = 0;
    if (!waterRunning) {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("LCD off. Waiting"));
      lcd.setCursor(0, 1); lcd.print(F("for command"));
    } else if (waterRunning) {
      displayWateringMessage();		
    }
    addTime = false; // reset button when lcd is off
  }
  
  //water countdown.. 
  if (waterRunning &&  (millis() - prevWaterOnTimer) > 60000){
		prevWaterOnTimer = millis();
		waterOnTimer -= 1;
		displayWateringMessage();		
	}  
 
}
void turnValOnOff(boolean turnOn, boolean manual) {
  if (turnOn) {
  //  getRainstatus();
    if ( rainValue > 1) { //not raining too hard, water
      digitalWrite(waterOnPin, LOW);  //TESTME
      //delay(50);
      //digitalWrite(waterOnPin, LOW);  //TESTME
      
      waterRunning = true;	  
	  waterOnTimer = waterOnInMins; //save the time for watering here for the timer. 
      waterInterval = (waterOnInMins * 60000);  //set in millis the time the vals should be open
      prevWaterMillis = millis(); //get the time we turned the water on for the auto shut off to be correct.
      if (manual) { //send message to the controller that the water  was turned On manually
        sendMessageToController(WATER_MANUAL_OVR, SENDING_CODE);
      }else {
		waterOnTimer -= 10; // remove 10 minutes to get the right time. Since the controller add 10 minutes for save guard.
	  }
    }
    else {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Raining."));
      lcd.setCursor(0, 1); lcd.print(F("Not watering"));
      delay(400);
      if (manual) { //send message to the controller that it won't turn on since it's raining
        sendMessageToController(RAIN_NO_ON, SENDING_CODE);
      }
    }
  } else if (!turnOn) {

    digitalWrite(waterOnPin, HIGH);  //TESTME
   
    waterOnInMins = 0;
    waterInterval = 0;
	waterOnTimer = 0;
    waterRunning = false;
	prevWaterOnTimer = 0;
    if (manual) { //send message to the controller that the water  was turned off manually
      sendMessageToController(WATER_TURNED_OFF, SENDING_CODE);
    }
  }
}
void getLDRstatus()
{ //Va0 = 5 * R1/(R1+R2)
  //R1 = 10k, R2 = 5k => Va0 = 5 * 10000/(10000 + 5000) = 5 * 10/15 = 3.33V
  //map return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  //0 = dark, 1=low light, 2=medium light, 3=full light
  ldrValue = map(analogRead(ldrPin), 0, 1023, 0, 10);
}
//void getRainstatus()
//{
 // rainValue = map(analogRead(rainSensorPin), 0, 1024, 0, 3);
//}
void turnLcdOn() {
  lcd.backlight();
  lcdOn = true;
  prevSleepLCDMillis = millis();
}
//reset sleep time for the LCD if a button has been pressed.
void actionByButtonResetLcdSleep() {
  prevSleepLCDMillis = millis();
}
//read command from wireless
void readCommand() {

  radio.read( &command, sizeof(char) * 20 );
  
  if (start){ //when self identifying, reset the clock
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
     lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Command "));lcd.setCursor(8, 0);lcd.print(sizeof(command));
    lcd.setCursor(0, 1); lcd.print(command);
  } else {
    command[0] = '\0'; command[1] = '\0';
  }
}
void sendMessageToController(char cmd, char confOrSend) {
  
  getLDRstatus();
 // getRainstatus();


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
  
  command[9] = END;
  command[10] = '\0';  
  
  radio.stopListening();

  radio.write( &command, sizeof(char) * 20);            // Send data, checking for error ("!" means NOT)
    
  radio.startListening();

}
void starting() { //when 1st starting , detect the controller

    unsigned long started_waiting_at; 
      
    uint8_t i = 0; //counter for number of retry.
    
    while (true) {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Searching for"));
      lcd.setCursor(0, 1); lcd.print(F("controller"));
      delay(100);  
      //sending identification to controller
      sendMessageToController(SELF_IDENTIFY, SENDING_CODE);

      started_waiting_at = micros();
      boolean timeout = false;
      while ( ! radio.available() ) {                            // While nothing is received
        if (micros() - started_waiting_at > 5000000 ) {        // If waited longer than 5 seconds, indicate timeout and exit while loop
          timeout = true;
          break;
        }
      }
      if (!timeout) { //confirmation received, continue
        readCommand(); //just read the confirmation
	if (dateRecvOk){
		lcd.clear(); lcd.setCursor(0, 0);
	        lcd.print(F("Controller found"));
	        lcd.setCursor(0, 1);
		lcd.print(F("Waiting for CMD"));
	        delay (700);
		 break;
	}else{
		lcd.clear(); lcd.setCursor(0, 0);	lcd.print(F("Error"));
		lcd.setCursor(0, 1);lcd.print(F("retrying"));
		delay(700);
		timeout = false;
	}
     } else {
        if (i > nbrOfRetry) {
          lcd.clear(); lcd.setCursor(0, 0);
          lcd.print(F("no cnrtl fnd."));
          lcd.setCursor(0, 1); lcd.print(F("Nbr of retry >60"));
		  radio.stopListening();
          radio.powerDown();
		  delay(2000);
		  break;
        } else {
          lcd.clear(); lcd.setCursor(0, 0);
          lcd.print(F("No Controller"));
          lcd.setCursor(0, 1); lcd.print(F("found. Retrying"));
          i = i + 1;
		  delay (1000);
        }        
      }
    }	
  } 
//***********************
//buttons on the left for manual override of the water.
void processButtons() {

  int btnWtrOnStatus = digitalRead(btnTurnWaterOnPin); //top button (on / off)
  int btnManSaveStatus = digitalRead(btnManualSavePin); //bottom button.

  if (!lcdOn && (btnWtrOnStatus == HIGH  || btnManSaveStatus == HIGH)) {
    turnLcdOn();
	delay(300);
  }

  if (btnWtrOnStatus == HIGH && !waterRunning ) {
    actionByButtonResetLcdSleep(); //reset lcd sleep timer if an action was done by a button
    if (!addTime) {
      waterOnInMins = 5;
      addTime = true;
    } else if (addTime && waterOnInMins < 999) {
      waterOnInMins += 5;
    }
    lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Set watering"));
    lcd.setCursor(0, 1); lcd.print(F("time: "));lcd.print(waterOnInMins);lcd.setCursor(11, 1);
    lcd.print(F("Min"));
    delay(400);
  }

  if (btnWtrOnStatus == HIGH && waterRunning) { //turn off if the water is running and button on/off is pressed

    actionByButtonResetLcdSleep(); //reset lcd sleep timer if an action was done by a button
    //ask for confirmation
    if (!turnOff) {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Turn off water?"));
      lcd.setCursor(0, 1); lcd.print(F("Btn1=Yes Btn2=No"));
      delay(400);
      turnOff = true;
    } else if (turnOff) {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Turning off"));
      lcd.setCursor(0, 1);
      lcd.print(F("Water"));
      delay(400);
      turnValOnOff(false, true);
      turnOff = false;
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Waiting for"));
      lcd.setCursor(0, 1); lcd.print(F("Command"));  
    }
  }
  //do not turn off the water.Continue
  if (btnManSaveStatus == HIGH && waterRunning  && turnOff) {
    turnOff = false;
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Water running"));
    lcd.setCursor(0, 1); lcd.print(F("Resuming"));
    delay(400);
  }
  //btn 2 pressed and water is not running so start it.
  if (btnManSaveStatus == HIGH && !waterRunning && waterOnInMins > 0) {
    actionByButtonResetLcdSleep(); //reset lcd sleep timer if an action was done by a button
    addTime = false;
    turnValOnOff(true, true);
	displayWateringMessage();
    delay(400);
  }
}
void displayWateringMessage(){
	lcd.clear(); lcd.setCursor(4, 0);
    lcd.print(F("Watering"));
	lcd.setCursor(0, 1);lcd.print(waterOnTimer);
	lcd.print(F("Min Remaining"));
}
void powerDown(){ 		  
        
	//clear any pending alarms 
    RTC.armAlarm(2, false); 
    RTC.clearAlarm(2); 
    RTC.alarmInterrupt(2, false);    
	RTC.setAlarm(ALM2_MATCH_HOURS, minute, hour, 0);   //set your wake-up time here 
    RTC.alarmInterrupt(2, true); 	 
	
	radio.stopListening(); //radio
	radio.powerDown();  
	lcd.noBacklight();

	delay(100);
	
	attachInterrupt(0, wakeUp, LOW); 
	
    LowPower.powerDown(SLEEP_FOREVER, ADC_OFF, BOD_OFF);		  
 
	detachInterrupt(0); 
	radio.powerUp();
	radio.startListening();
	
	RTC.armAlarm(2, false); 
    RTC.clearAlarm(2); 
    RTC.alarmInterrupt(2, false); 
	
}


