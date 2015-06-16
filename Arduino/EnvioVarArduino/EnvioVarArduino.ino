#include <PinChangeInt.h>
#include <SoftwareSerial.h>
#include <Event.h>
#include <Timer.h>
#include <eHealth.h>

Timer timer; 
SoftwareSerial Bluetooth(2, 3); // RX | TX
int cont =0;
char delimitador = '+';
char finPaquete = '~';

void setup()
   { 
     Bluetooth.flush();
     delay(500);
     Serial.begin(9600);
     Bluetooth.begin(9600);
     eHealth.initPulsioximeter();
     timer.every(7000, enviarVariables);
     PCintPort::attachInterrupt(6, readPulsioximeter, RISING);
     delay(100);
   }

void loop(){  
     timer.update();         
}
   
void enviarVariables()
{
    if(Bluetooth.isListening()){
    Bluetooth.print("#"); 
    Bluetooth.print(delimitador);
    Bluetooth.print(eHealth.getBPM());
    Bluetooth.print(delimitador);  
    Bluetooth.print(eHealth.getOxygenSaturation());
    Bluetooth.print(delimitador);
    Bluetooth.print(eHealth.getTemperature());
    Bluetooth.print(delimitador); 
    Bluetooth.print(eHealth.getSkinConductance());
    Bluetooth.print(delimitador); 
    Bluetooth.print(eHealth.getSkinResistance());
    Bluetooth.print(finPaquete);
   }
}

void readPulsioximeter() {  
  cont ++;
  if (cont == 50) { //Get only one of  50 measures to reduce the latency
    eHealth.readPulsioximeter();  
    cont = 0;
  }
}
