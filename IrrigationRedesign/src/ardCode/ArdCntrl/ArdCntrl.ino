
/*--reserved pins that are not declared here--*/
//13, 11 and 12 are also reserved for the radio  http://theblogofpeterchen.blogspot.ca/2015/02/english-text-string-wireless.html

#include <SPI.h>   // Comes with Arduino IDE
#include "RF24.h"  // Download and Install (See above)

//communication data structure
const char START        	 	= 'z';//start of command
const char SENDING_CODE      	= 's';
const char END          	 	= 'e'; //end of command
//from controller
const char OPEN_WATER       	= 'o';  //command to send to open the valve
const char CLOSE_WATER      	= 'a';  //command to send to close the valve
const char GET_SENSOR_DATA  	= 'g';  //command to send to get the status
//from worker
const char SELF_IDENTIFY    	= 'i';  //code that the worker is self identifying to the controller.
const char REVC_CONFIRM_CODE  	= 'c';  //confirmation from the worker.
const char WATER_MANUAL_OVR	  	= 'm'; 	//code that the worker send that tells the controller that the water has been turned on manually
const char WATER_TURNED_OFF		  = 'w';
//example1: zosg123e : z = start, o = open water, s=sending , 123 = workerId, 132 = nbr of minutes to keep the valve open, e=end

char command[20];

String cntrlCmd;
/*-----( Declare objects )-----*/
/* Hardware configuration: Set up nRF24L01 radio on SPI bus plus (usually) pins 7 & 8 (Can be changed) */
RF24 radio(7, 8);

/*-----( Declare Variables )-----*/
byte pipe[][6] = {"mst02", "wk003"}; // These will be the names of the "Pipes"

void setup() {
  cntrlCmd.reserve(20);
  Serial.begin(9600); // Initialize serial port
  //initialize wireless communication
  radio.begin();          // Initialize the nRF24L01 Radio
  radio.setChannel(108);  // Above most WiFi frequencies
  radio.setDataRate(RF24_250KBPS); // Fast enough.. Better range
  radio.setRetries(15, 15);
  radio.setCRCLength(RF24_CRC_16);
  // RF24_PA_MAX is default.
  // PALevelcan be one of four levels: RF24_PA_MIN, RF24_PA_LOW, RF24_PA_HIGH and RF24_PA_MAX
  //radio.setPALevel(RF24_PA_LOW);
  radio.setPALevel(RF24_PA_MAX);
  // Open a writing and reading pipe on each radio, with opposite addresses
  //radio.openWritingPipe(pipe[1]);
  radio.openReadingPipe(1, pipe[0]);

  // Start the radio listening for data
  radio.startListening();
  
  while (!Serial) {
     ; // wait for serial port to connect. Needed for native USB
   }
  
  
}
void loop() {

  if ( radio.available())
  {  
	  initCommand();
	
    while (radio.available())   // While there is data ready to be retrieved from the receive pipe
    {
      radio.read( &command, sizeof( char) * 20 );             // Get the data
    }
    Serial.print(command);
  }

  while (Serial.available()) {
    cntrlCmd = Serial.readStringUntil("e");
  }

  if (cntrlCmd != "" && cntrlCmd.charAt(0) == START && cntrlCmd.charAt(cntrlCmd.length() - 1) == END)
  {
    initCommand();
  
    cntrlCmd.toCharArray(command, 20);

    //set the pipe to the right receiver.
    pipe[1][2] = command[3];
    pipe[1][3] = command[4];
    pipe[1][4] = command[5];

    radio.stopListening();
    radio.openWritingPipe(pipe[1]);

    if (!radio.write( &command, sizeof(char) * 20)) {            // Send data, checking for error ("!" means NOT)
      Serial.print(F("failed:"));
      Serial.println(cntrlCmd);
    }

    radio.startListening();
    cntrlCmd = "";
  } else if (cntrlCmd.length()  > 0) {
    Serial.print(F("Incomplete:"));
    Serial.println(cntrlCmd);
    cntrlCmd = "";
  }
}

void initCommand(){

    for(int i = 0 ; i < sizeof(command) ; i++){
      command[i] = '\0';
    }
    
}

