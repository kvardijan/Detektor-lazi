#include "DHT.h"
#include "BluetoothSerial.h"

BluetoothSerial SerialBT;
String poruka = "";

int dhtPin = 27;
int analogOtpor = A0;
int analogPuls = A3;

DHT dht(dhtPin,DHT22);
float temperatura,vlaznost;

int otpor, otporVrijednost;
float voltaza = 0;

int brojacPulsa, rastBroj = 0, otkucaj;
float pocetak, sada;
float ocitanje, proslaPulsVrijednost = 0;
float prva, druga, treca, vrijednost;
bool raste = false, zavrseno = false;

void setup(){
  Serial.begin(9600);
  SerialBT.begin("Detektor laži");
  dht.begin();
}

void loop(){
  poruka = "";
  izmjeriPuls();
  izmjeriOtpor();
  izmjeriTempVlaz();
  Serial.println(poruka);
  SerialBT.println(poruka);
  delay(100);
}

void izmjeriTempVlaz(){
  do{
    temperatura = dht.readTemperature();
    vlaznost = dht.readHumidity();
  }while(isnan(temperatura) || isnan(vlaznost));

  poruka += "T:";
  poruka += temperatura;
  poruka += ";";
  poruka += "H:";
  poruka += vlaznost;
  poruka += ";";
}

void izmjeriOtpor(){
  otporVrijednost = analogRead(analogOtpor);
  voltaza = (otporVrijednost * 3.3)/4095.0;
  otpor = 950000 * ((3.3/voltaza) - 1);
  poruka += "R:";
  poruka += otpor;
  poruka += ";";
}

void izmjeriPuls(){
  zavrseno = false;
  poruka += "BPM:";
  while(zavrseno == false){
    brojacPulsa = 0;
    pocetak = millis();
    ocitanje = 0.;
    do{
      ocitanje += analogRead(analogPuls);
      brojacPulsa++;
      sada = millis();
    }while (sada < pocetak + 20);  
    ocitanje /= brojacPulsa;

    if (ocitanje > proslaPulsVrijednost)
    {
      rastBroj++;
      if (!raste && rastBroj > 4)
      {

        raste = true;
        prva = millis() - otkucaj;
        otkucaj = millis();

        vrijednost = 60000. / (0.4 * prva + 0.3 * druga + 0.3 * treca);

        treca = druga;
        druga = prva;
        if(vrijednost > 40 && vrijednost < 140){
          
          poruka += vrijednost;
          zavrseno = true;
        }
        
      }
    }
    else
    {
      raste = false;
      rastBroj = 0;
    }

    proslaPulsVrijednost = ocitanje;

    delay(1);
  }
  poruka += ";";
}


