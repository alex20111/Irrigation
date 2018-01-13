#include <LowPower.h>
//Global variables:
/*--reserved pins that are not declared here--*/
//Pin a4 and a5 (analogue) are reserved for the LCD
//13, 11 and 12 are also reserved for the radio
//0, 1, 2 is for the clock  --> http://www.rinkydinkelectronics.com/library.php?id=5

/*-----( Import needed libraries )-----*/
#include <SPI.h>   // Comes with Arduino IDE
#include "RF24.h"  // Download and Install (See above)
#include <Wire.h>  // Comes with Arduino IDE
#include <LiquidCrystal_I2C.h>
#include <RTClibExtended.h> 

RTC_DS3231 rtc;

/*-----( Declare objects )-----*/
/* Hardware configuration: Set up nRF24L01 radio on SPI bus plus (usually) pins 7 & 8 (Can be changed) */
RF24 radio(7, 8);

/*----LCD-------*/
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);  // Set the LCD I2C address

// Init a Time-data structure
DateTime t;

//pins buttons
#define btnCntrlHeadless   	3 //decide if running with controller or not  - ok
#define ldrPin				A0 // pin for the Light dependant resistance. ok
#define rainSensorPin		A1 //pin for the rain sensor
#define waterOnPin			4 // pin for turning on the solenoid (Water) ok

//buttons to turn water manually.
#define btnTurnWaterOnPin  	9 //btn to turn on the water manually
#define btnManualSavePin   	5 //btn to save the settings for btnTurnWaterOnPin

//headless pins
#define btnHSetPin			6
#define btnHenterPin		A2
#define btnHCancelPin		A3

/*-----------Communication format constants-------------------------*/
const char workerId[4] = "003";
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
const char RAIN_NO_ON			= 'r';//code that the worker send that tells the controller that no watering is done since it's raining
const char WOKE_UP         = 'd';//code that the worker send that tells the controller that the worker woke up

static  const uint8_t monthDays[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

//with controller values
char command[20];
boolean waterRunning 	= false;  //if it's watering

//-----headless values-----//
uint8_t schedType		= 10; //the schedule type that will be selected.
uint8_t schedSet 		= 10;
uint8_t schedTime  		= 10;
uint8_t headlessOption  = 5;
int year			= 2016;
uint8_t month			= 8;
uint8_t day				= 5;
uint8_t hour			= 0;
uint8_t minute			= 0;
uint8_t seconds			= 0;
boolean btnSaveActive = false;
boolean rightBtnNotActive = true;
boolean leftBtnNotActive = true;

//------end------//
//common values
boolean turnOff = false; // boolean to decide if we turn off the water manually when it's running
uint8_t ldrValue			= 0;  //Test
uint8_t rainValue 			= 0;
boolean controller 		= false; //if the worker is controlled by the controller
boolean addTime 		= false; //Init of the add time of the manual turn on water.
int waterOnInMins 		= 0; // number of minutes that the water must be on
int waterOnTimer		= 0; //timer that will display how many minutes are remaining to water on the LCD.
unsigned long prevWaterOnTimer = 0;//to display watering countdown.. always needs to start a 0 so it will display the number of minutes right away


unsigned long waterInterval 	 = 0; // number of milliseconds that the water must be on
unsigned long prevSleepLCDMillis = 0; // number of milliseconds that the LCD is on (5 min)
unsigned long prevWaterMillis = 0;
unsigned int  lcdInterval 		= 30000; // number of milliseconds that the LCD is on (30sec)
boolean lcdOn 			= true; //tell if when pressing any buttons if we should turn the lcd on.
char buffer[3]; // for conversion
String bufferString; //conversion buffer from int to char

uint8_t nbrOfRetry    = 60; //number of retry before stopping to find controller.

/*-----( Declare Radio Variables )-----*/
byte addresses[][6] = {"mst02", "wk003"}; // These will be the names of the "Pipes"// must be "wk" and worker id --> const char workerId[4] = "001";

boolean start = true;
boolean dateRecvOk = false;

 void wakeUp()        // here the interrupt is handled after wakeup 
 { 
 } 


void setup() {
  bufferString.reserve(10);
  start = true;
  pinMode(waterOnPin, OUTPUT); //set pin as output for watering
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
  
  
   rtc.begin();      

   //Set SQW pin to OFF (in my case it was set by default to 1Hz) 
   //The output of the DS3231 INT pin is connected to this pin 
   //It must be connected to arduino D2 pin for wake-up 
    rtc.writeSqwPinMode(DS3231_OFF);  
	rtc.armAlarm(1, false); 
    rtc.clearAlarm(1); 
    rtc.alarmInterrupt(1, false);   

  lcd.clear(); lcd.setCursor(0, 0);
  lcd.print(F("Starting"));
  delay(400);  
}

void loop() {

  if (start) {
    starting();
    start = false;
  }
  if (controller) {

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
  } else if (!controller) {
    if (headlessOption == 5) {
      lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Set Clock"));
      lcd.setCursor(0, 1); lcd.print(F("B1=V B2=Y B3=N"));
      headlessOption = 10; //display the start message then continue on the config.
    } else if (headlessOption == 15) { //display the clock message if user choose to set the time.
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(year); lcd.print("/"); lcd.print(month); lcd.print("/"); lcd.print(day);
      lcd.setCursor(0, 1); lcd.print(hour); lcd.print(":"); lcd.print(minute);
      delay(1000);
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Year: ")); lcd.print(year);
      headlessOption = 20;
    } else if (headlessOption == 25) { //set message to set the schedule
      headlessOption = 30; //set of everything is ok and wait  for user to set schedule.
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("please set schedule"));
      lcd.setCursor(0, 1); lcd.print(F("Btn1 = Set sched"));
    }
    if (leftBtnNotActive) {
      processHeadlessControls();
    }
    if (headlessOption >= 40) {	//schedule is set and ready to monitor.
      processHeadlessTimer();
      if (headlessOption == 50) { //if we trigger a message to display
        if (waterRunning) {
          displayWateringMessage();
        } else {
          printSchedule();
        }
        headlessOption = 40;
      }
    }
  }

  if (rightBtnNotActive) { //only process the left buttons if we are not setting a schedule..
    processButtons(); //process the buttons that turn on/off the water manually
  }

  //**********************//
  //automatically turn valve off --> if from controller, 10 min has been added.
  if (waterRunning && (millis() - prevWaterMillis) > waterInterval) {
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Auto turn off"));
    delay(800);
    turnValOnOff(false, true);
    //message to lcd if we are in controller mode
    if (controller) {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Waiting for"));
      lcd.setCursor(0, 1); lcd.print(F("Command"));
    } else {
      if (headlessOption == 40) {
        headlessOption = 50; //display schedule
      }
    }
  }

  //automatically turn lcd off
  if (lcdOn && (millis() - prevSleepLCDMillis) > lcdInterval) {
    lcd.noBacklight();
    //lcd.noDisplay(); DEBUG
    //turn lcd off
    lcdOn = false;
    if (controller ) { //display generic message when lcd is off. Except when it's watering
      waterOnInMins = 0;
      if (!waterRunning) {
        lcd.clear(); lcd.setCursor(0, 0);
        lcd.print(F("LCD off. Waiting"));
        lcd.setCursor(0, 1); lcd.print(F("for command"));
      } else if (waterRunning) {
        displayWateringMessage();		
      }
    } else if (!controller) {
      rightBtnNotActive = true;
      if (headlessOption == 30) {
        lcd.clear(); lcd.setCursor(0, 0);
        lcd.print(F("please set schedule"));
        lcd.setCursor(0, 1); lcd.print(F("Btn1 = Setsched"));
      } else if (headlessOption == 40) {
        headlessOption = 50; //display schedule
      }
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
    getRainstatus();
    if ( rainValue > 1) { //not raining too hard, water
      digitalWrite(waterOnPin, HIGH);
      waterRunning = true;	  
	  waterOnTimer = waterOnInMins; //save the time for watering here for the timer. 
      waterInterval = (waterOnInMins * 60000);  //set in millis the time the vals should be open
      prevWaterMillis = millis(); //get the time we turned the water on for the auto shut off to be correct.
      if (controller && manual) { //send message to the controller that the water  was turned On manually
        sendMessageToController(WATER_MANUAL_OVR, SENDING_CODE);
      }else if (controller){
		waterOnTimer -= 10; // remove 10 minutes to get the right time. Since the controller add 10 minutes for save guard.
	  }
    }
    else {
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Raining."));
      lcd.setCursor(0, 1); lcd.print(F("Not watering"));
      delay(400);
      if (controller && manual) { //send message to the controller that it won't turn on since it's raining
        sendMessageToController(RAIN_NO_ON, SENDING_CODE);
      }
    }
  } else if (!turnOn) {
    digitalWrite(waterOnPin, LOW);
    waterOnInMins = 0;
    waterInterval = 0;
	waterOnTimer = 0;
    waterRunning = false;
	prevWaterOnTimer = 0;
    if (controller && manual) { //send message to the controller that the water  was turned off manually
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
void getRainstatus()
{
  rainValue = map(analogRead(rainSensorPin), 0, 1024, 0, 3);
}
void turnLcdOn() {
  lcd.backlight();
  //lcd.display(); DEBUG
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
  
	rtc.adjust(DateTime(year, month, day, hour, minute, seconds));
  t = rtc.now(); 
	printDate(); //DEBUG
	delay(3000);
	  
	dateRecvOk = true;
  }  

  if (command[0] != ' ' && command[2] == SENDING_CODE &&
      command[3] == workerId[0] && command[4] == workerId[1] &&
      command[5] == workerId[2]) {
    // printOnLcd("Command Recv", command, 500); //DEBUG
  } else {
    command[0] = '\0'; command[1] = '\0';
  }
}
void sendMessageToController(char cmd, char confOrSend) {

  getLDRstatus();
  getRainstatus();

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
  bufferString = String(rainValue);
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
  

  radio.write( &command, sizeof(char) * 20);    
  radio.startListening();
}
void starting() {

  int mode = digitalRead(btnCntrlHeadless); //read button

  if (mode == LOW) {
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
			lcd.clear(); lcd.setCursor(0, 0);lcd.print(F("Controller found"));
			lcd.setCursor(0, 1);lcd.print(F("Waiting for CMD"));
			delay (700);
			controller = true;
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
          lcd.setCursor(0, 1); lcd.print(F("Nbr of retry >30"));
		  radio.stopListening();
          radio.powerDown();
		  controller = false;
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
  }else {
	controller = false;
  }
  
  if (!controller) {
	// Set the clock to run-mode, and disable the write protection
	  if (rtc.lostPower()) {
		rtc.adjust(DateTime(2016, 8, 1, 12, 0, 0));
	  }

	delay(100);

	t = rtc.now();
	
    radio.stopListening();
    radio.powerDown();
  }
}
//***********************
//Global for headless and controlled
//buttons on the left for manual override of the water.
void processButtons() {

  int btnWtrOnStatus = digitalRead(btnTurnWaterOnPin); //top button (on / off)
  int btnManSaveStatus = digitalRead(btnManualSavePin); //bottom button.

  if (!lcdOn && (btnWtrOnStatus == HIGH  || btnManSaveStatus == HIGH)) {
    turnLcdOn();
	delay(300);
  }

  if (btnWtrOnStatus == HIGH && !waterRunning ) {
    leftBtnNotActive = false;
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
      leftBtnNotActive = true;
      lcd.clear(); lcd.setCursor(0, 0);
      lcd.print(F("Turning off"));
      lcd.setCursor(0, 1);
      lcd.print(F("Water"));
      delay(400);
      turnValOnOff(false, true);
      turnOff = false;
      if (controller) {
        lcd.clear(); lcd.setCursor(0, 0);
        lcd.print(F("Waiting for"));
        lcd.setCursor(0, 1); lcd.print(F("Command"));
      } else if (headlessOption == 40) {
        headlessOption = 50; //display next schedule if schedule is active.
      }
    }
  }
  //do not turn off the water.Continue
  if (btnManSaveStatus == HIGH && waterRunning  && turnOff) {
    leftBtnNotActive = true;
    turnOff = false;
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Water running"));
    lcd.setCursor(0, 1); lcd.print(F("Resuming"));
    delay(400);
  }
  //btn 2 pressed and water is not running so start it.
  if (btnManSaveStatus == HIGH && !waterRunning && waterOnInMins > 0) {
    leftBtnNotActive = true;
    actionByButtonResetLcdSleep(); //reset lcd sleep timer if an action was done by a button
    addTime = false;
    turnValOnOff(true, true);
	displayWateringMessage();
    delay(400);
  }
}

void processHeadlessControls() {

  int btnOne = digitalRead(btnHSetPin);
  int btnTwo = digitalRead(btnHenterPin);
  int btn3 = digitalRead(btnHCancelPin);

  if (!lcdOn && (btnOne == HIGH  || btnTwo == HIGH || btn3 == HIGH)) {
    turnLcdOn();
  }
  /*---------------------------------------*/
  /*----------------Start -----------------*/
  //display date time if asked
  if (btnOne == HIGH && headlessOption == 10) {
	headlessOption = 5; //re-display the start options.
    printDate(); 
	delay(1000);
  }
  //set the clock
  if (btnTwo == HIGH && headlessOption == 10) {
    actionByButtonResetLcdSleep();
    headlessOption = 15;
    schedTime = 50;
    rightBtnNotActive = false;
    delay(400);
  }
  //date is ok, do not change anything.
  if (btn3 == HIGH && headlessOption == 10) {
    actionByButtonResetLcdSleep();
    headlessOption = 25;
    rightBtnNotActive = false;
    printDate();
    delay(2000);
  }
  /*----------------------Set clock ----------------------*/
  /*------------------------------------------------------*/
  if (btnOne == HIGH && headlessOption == 20) {
    actionByButtonResetLcdSleep();
    lcd.clear(); lcd.setCursor(0, 0);
    if (schedTime == 50) {
      year += 1;
      lcd.print(F("Year: ")); lcd.print(year);
    } else if (schedTime == 60) {
      if (month > 11) {
        month = 1;
      } else {
        month += 1;
      }
      lcd.print(F("Month: ")); lcd.print(month);
    } else if (schedTime == 70) {
      addDayNoRoll(1);
      lcd.print(F("Day: ")); lcd.print(day);
    } else if (schedTime == 80) {
      if (hour > 22) {
        hour = 0;
      } else {
        hour += 1;
      }
      lcd.print(F("Hour: ")); lcd.print(hour);
    } else if (schedTime == 90) {
      if (minute > 58) {
        minute	= 1;
      } else {
        minute	+= 1;
      }
      lcd.print(F("Minute: ")); lcd.print(minute);
    }
	lcd.setCursor(0, 1);lcd.print(F("B1:+ B2:> B3=Sav"));
	
    rightBtnNotActive = false;
    delay(400);
  }
  if (btnTwo == HIGH && headlessOption == 20) { //switch between year, month, day, hours.
    actionByButtonResetLcdSleep();
    lcd.clear(); lcd.setCursor(0, 0);
    if (schedTime == 50) {
      lcd.print(F("Month: ")); lcd.print(month);
      schedTime = 60;
    } else if (schedTime == 60) {
      lcd.print(F("Day: ")); lcd.print(day);
      schedTime = 70;
    } else if (schedTime == 70) {
      lcd.print(F("Hour: ")); lcd.print(hour);
      schedTime = 80;
    } else if (schedTime == 80) {
      lcd.print(F("Minute: ")); lcd.print(minute);
      schedTime = 90;
    } else if (schedTime == 90) {
      lcd.print(F("Year: ")); lcd.print(year);
      schedTime = 50;
    }
	lcd.setCursor(0, 1);lcd.print(F("B1:+ B2:> B3=Sav"));
    rightBtnNotActive = false;
    delay(400);
  }
  /*Save the clock to the ram and display the set schedule option.*/
  if (btn3 == HIGH && headlessOption == 20) {
    actionByButtonResetLcdSleep();
    headlessOption = 25;
    rtc.adjust(DateTime(year, month, day, hour, minute, 0));
    t = rtc.now();

    hour = 0; month = 0; day = 0; hour = 0; minute = 0; //reset variables.
    schedTime = 10; // to start the schedule at the right spot.
    lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Clock set."));
    lcd.setCursor(0, 0); lcd.print(F("New Time."));
    delay(2000);
    printDate(); //display new date
    delay(3000);
    rightBtnNotActive = false;

  }
  /*----------------------------------------------------*/
  /*----------------- SCHEDULE -------------------------*/
  /*----------------------------------------------------*/
  /*select schedule type until the 2nd button is pressed*/
  if (btnOne == HIGH && schedTime == 10 && headlessOption == 30) {
    actionByButtonResetLcdSleep();
    lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Options"));
    if (schedSet == 10) {
      lcd.setCursor(0, 1); lcd.print(F("Daily"));
      schedSet = 20; schedType = 10;
      btnSaveActive = true;
    } else if (schedSet == 20) {
      lcd.setCursor(0, 1); lcd.print(F("Every 2 days"));
      schedSet = 30; schedType = 20;
    } else if (schedSet == 30) {
      lcd.setCursor(0, 1); lcd.print(F("Every 3 days"));
      schedSet = 40; schedType = 30;
    } else if (schedSet == 40) {
      lcd.setCursor(0, 1); lcd.print(F("Every week"));
      schedSet = 10; schedType = 40;
    }
    rightBtnNotActive = false;
    delay(400);

  }
  /*Increment the hours*/
  if (btnOne == HIGH && schedTime == 20 && headlessOption == 30) {
    actionByButtonResetLcdSleep();
    if (hour > 22) {
      hour = 0;
    }
    else {
      hour += 1;
    }
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Schedule Time"));
    lcd.setCursor(0, 1);
    lcd.print(hour); lcd.print(":"); lcd.print(minute);
    rightBtnNotActive = false;	
    delay(400);
  }
  /*increment the minutes*/
  if (btnOne == HIGH && schedTime == 30 && headlessOption == 30) {
    actionByButtonResetLcdSleep();
    if (minute > 58) {
      minute = 1;
    }
    else {
      minute += 1;
    }
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Schedule Time")); lcd.setCursor(0, 1);
    lcd.print(hour); lcd.print(":"); lcd.print(minute);
    rightBtnNotActive = false;
    delay(400);
  }
  /*select how long the water will run for*/
  if (btnOne == HIGH && schedTime == 40 && headlessOption == 30) {
    actionByButtonResetLcdSleep();
    waterOnInMins += 5;
    if (waterOnInMins >= 999) {
      waterOnInMins = 5;
    }
    lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Water for: "));
    lcd.setCursor(0, 1); lcd.print(waterOnInMins);
    lcd.setCursor(4, 1); lcd.print(F("Minutes"));
    rightBtnNotActive = false;
    delay(400);
  }
  //toggle between option :schedule / hours / minutes / runtime
  if (btnTwo == HIGH && btnSaveActive && headlessOption == 30) {
    rightBtnNotActive = false;
    actionByButtonResetLcdSleep();
    if (schedTime == 10) {
      schedTime = 20; //Schedule hours
      lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Schedule Time"));
      lcd.setCursor(0, 1); lcd.print(hour); lcd.print(":"); lcd.print(minute);
    }
    else if (schedTime == 20) {
      schedTime = 30; //Schedule Minutes
    }
    else if (schedTime == 30) {
      lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Water for: "));
      lcd.setCursor(0, 1); lcd.print(waterOnInMins);
      lcd.setCursor(4, 1); lcd.print(F("Minutes"));
      schedTime = 40; //set the runtime .
    } else if (schedTime == 40) { //save and reset values
      schedTime = 10;
      schedSet = 10;

      //set year, month, day for compare.
      t = rtc.now();
      year = t.year();
      month = t.month();
      day = t.day();

      if (isDateAtSchedule()) { //if the scheduled date is  before the current date, add a day,
        //verify what schedule it is and add the days
        addDaysToSchedule();
      }
      headlessOption = 50; //set to schedule started. and display message
      btnSaveActive = false;
      rightBtnNotActive = true;
    }

    delay(400);
  }
  //button to cancel schedule definition and start again*/
  /*---- if schedule is running cancel it and ask to re-schedule*/
  if (btn3 == HIGH && headlessOption >= 30) {
    actionByButtonResetLcdSleep();
    headlessOption = 25; //re-display the message to set the schedule
    lcd.clear(); lcd.setCursor(0, 0);
    lcd.print(F("Cancelling") ); lcd.setCursor(0, 1);
    lcd.print(F("Schedule") );
    delay(2000);
    schedTime = 10; schedSet = 10;
    minute = 0; hour = 0; year = 0; month = 0; day = 0; schedType = 10;
    btnSaveActive = false;
    rightBtnNotActive = true;
    if (waterRunning) {
      turnValOnOff(false, false);
    }
  }
  if (btn3 == HIGH && !btnSaveActive && headlessOption == 30) {
    printDate(); //TODO debug
  }
}
void processHeadlessTimer() {

  if (!waterRunning && isDateAtSchedule()) {

    turnValOnOff(true, false);
    //adjust new time depending on schedule.
    addDaysToSchedule();
    headlessOption = 50; // display schedule message
  }
}
//return true when the current date is = or greater than the set schedule date.
boolean isDateAtSchedule() {

  t = rtc.now();
  if (t.year() == year) {
    if (t.month() == month) {
      if (t.day() == day ) {
        if ( t.hour() == hour) {
          if (t.minute() == minute ) {
            return  true;
          } else if (t.minute() < minute ) {
            return  false;
          } else {
            return  true;
          }
        } else if (t.hour() < hour) {
          return  false;
        } else {
          return  true;
        }
      } else if (t.day() < day  ) {
        return  false;
      } else {
        return  true;
      }
    } else if (t.month() < month ) {
      return  false;
    } else {
      return  true;
    }
  } else if (t.year() < year ) {
    return  false;
  } else {
    return  true;
  }
}
/*print date from clock*/
void printDate() {
  // Display time centered on the upper line
  lcd.clear();
  lcd.setCursor(4, 0);
  lcd.print(t.year()); lcd.print(F("/")); lcd.print(t.month()); lcd.print(F("/")); lcd.print(t.day());
  lcd.setCursor(6, 1);
  lcd.print(t.hour()); lcd.print(F(":")); lcd.print(t.minute()); lcd.print(F(".")); lcd.print(t.second());

}
/*print schedule from working storage*/
void printSchedule() {
  lcd.clear(); lcd.setCursor(0, 0); lcd.print(F("Next ")); lcd.print(year); lcd.print("/"); lcd.print(month); lcd.print("/"); lcd.print(day);
  lcd.setCursor(0, 1); lcd.print(F("Sched   ")); lcd.print(hour); lcd.print(":"); lcd.print(minute);
}
/*Verify when we are adding a day that we don't go over the 31 or 30*/
void addDay(uint8_t dayToAdd) {

  day += dayToAdd;
  //verify if we roll over or not.
  if (day > monthDays[t.month() - 1]) { //roll over
    month = t.month() + 1;
    day = day - monthDays[t.month() - 1];
  }
}
void addDayNoRoll(uint8_t dayToAdd) {

  day += dayToAdd;
  //verify if we roll over or not.
  if (day > monthDays[t.month() - 1]) { //roll over
    day = 1;
  }
}

void addDaysToSchedule() {
  if (schedType == 10) {
    addDay(1);
  } else if (schedType == 20) {
    addDay(2);
  }
  else if (schedType == 30) {
    addDay(3);
  }
  else if (schedType == 40) {
    addDay(7);
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
    rtc.armAlarm(2, false); 
    rtc.clearAlarm(2); 
    rtc.alarmInterrupt(2, false);    
	rtc.setAlarm(ALM2_MATCH_HOURS, minute, hour, 0);   //set your wake-up time here 
    rtc.alarmInterrupt(2, true); 	 
	
	radio.stopListening(); //radio
	radio.powerDown();  

	//battery 
	lcd.noBacklight();
    //lcd.noDisplay(); 
	delay(100);
	
	attachInterrupt(0, wakeUp, LOW); 
	
    LowPower.powerDown(SLEEP_FOREVER, ADC_OFF, BOD_OFF);		  
 
	detachInterrupt(0); 
	radio.powerUp();
	radio.startListening();
	
	rtc.armAlarm(2, false); 
    rtc.clearAlarm(2); 
    rtc.alarmInterrupt(2, false); 
	
	
}
