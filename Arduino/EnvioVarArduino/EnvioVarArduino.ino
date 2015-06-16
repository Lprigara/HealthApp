#include <PinChangeInt.h>
#include <Event.h>
#include <Timer.h>
#include <eHealth.h>

Timer timer; 
int cont =0;
char delimitador = '+';
char finPaquete = '~';

void setup()
   { 
     Serial.begin(9600);
     eHealth.initPulsioximeter();
     timer.every(7000, enviarVariables)
     PCintPort::attachInterrupt(6, readPulsioximeter, RISING);
   }

void loop(){  
     timer.update();         
}
   
void enviarVariables()
{
    Serial.print("#"); 
    Serial.print(delimitador);
    Serial.print(eHealth.getBPM());
    Serial.print(delimitador);  
    Serial.print(eHealth.getOxygenSaturation());
    Serial.print(delimitador);
    Serial.print(eHealth.getTemperature());
    Serial.print(delimitador); 
    Serial.print(eHealth.getSkinConductance());
    Serial.print(delimitador); 
    Serial.print(eHealth.getSkinResistance());
    Serial.print(finPaquete);
   }
}

void readPulsioximeter() {  
  cont ++;
  if (cont == 50) { //Get only one of  50 measures to reduce the latency
    eHealth.readPulsioximeter();  
    cont = 0;
  }
}
