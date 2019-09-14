//CODE FOR MICROCONTROLLER

int load1 =12;
int load2 =10;
int load3 =8;
int load4=7;

int receive=0;

int load1_state=0;
int load2_state=0;
int load3_state=0;
int load4_state=0;

String command;

void setup() {
    
Serial.begin(9600);
pinMode(load1,OUTPUT);
pinMode(load2,OUTPUT);
pinMode(load3,OUTPUT);
pinMode(load4,OUTPUT);


}

void loop() {
  // put your main code here, to run repeatedly:
  /*if(Serial.available()){
    char c=Serial.read();
    command +=c;*/
 if(Serial.available()>0)
 {
  receive=Serial.read();
  delay (20);
  }   
  
    //RELAY 1
  if(load1_state == 0 && receive == '1')
  {
    digitalWrite(load1,HIGH);
    load1_state=1;
    receive=0;
    }

    if(load1_state ==1 && receive == '1')
  {
    digitalWrite(load1,LOW);
    load1_state=0;
    receive=1;
    }
      //RELAY 2

     if(load2_state == 0 && receive == '2')
  {
    digitalWrite(load2,HIGH);
    load2_state=1;
    receive=0;
    }

    if(load2_state ==1 && receive == '2')
  {
    digitalWrite(load2,LOW);
    load2_state=0;
    receive=1;
    }

      //RELAY 3

     if(load3_state == 0 && receive == '3')
  {
    digitalWrite(load3,HIGH);
    load3_state=1;
    receive=0;
    }

    if(load3_state == 1 && receive == '3')
  {
    digitalWrite(load3,LOW);
    load3_state=0;
    receive=1;
    }

      //RELAY 4

     if(load4_state == 0 && receive == '4')
  {
    digitalWrite(load4,HIGH);
    load4_state=1;
    receive=0;
    }

    if(load3_state == 1 && receive == '4')
  {
    digitalWrite(load4,LOW);
    load4_state=0;
    receive=1;
    }
  }
