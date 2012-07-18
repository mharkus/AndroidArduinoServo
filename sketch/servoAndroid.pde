#include <Servo.h>
#include <Usb.h>
#include <AndroidAccessory.h>

Servo servo;

AndroidAccessory acc("Marc Tan",
		     "Seeduino",
		     "Servo Seeduino Board",
		     "1.0",
		     "http://www.marctan.com",
		     "0000000012345678");


void setup(){
  Serial.begin(115200);
  acc.powerOn();
      
  servo.attach(8);
}

void loop(){
      byte msg[1];

      if (acc.isConnected()) {
            int len = acc.read(msg, sizeof(msg), 1);
            if(len > 0){
                   servo.write(msg[0]);
            }
      }else{
            servo.write(0); // reset to 0 degree position
      }
      
      delay(15);
}

