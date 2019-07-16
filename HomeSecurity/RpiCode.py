from picamera import PiCamera
from time import sleep
import smtplib
import time
from datetime import datetime
from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
import RPi.GPIO as GPIO
import time

toaddr = 'recapp.12567@gmail.com'
me = 'senderpi1245@gmail.com'
Subject='Security Alert!!'

GPIO.setmode(GPIO.BCM)

P=PiCamera()
P.resolution= (1024,768)
P.start_preview()
    
GPIO.setup(23, GPIO.IN)
while True:
    if GPIO.input(23):
        print("Motion detected...")
        #camera warm-up time
        P.capture('movement.jpg')
        time.sleep(7)
        subject='Security allert!!'
        msg = MIMEMultipart()
        msg['Subject'] = subject
        msg['From'] = me
        msg['To'] = toaddr
        
        fp= open('movement.jpg','rb')
        img = MIMEImage(fp.read())
        fp.close()
        msg.attach(img)

        server = smtplib.SMTP('smtp.gmail.com',587)
        server.starttls()
        server.login(user = 'recapp.12567@gmail.com',password='recapp#12@')
        server.send_message(msg)
        server.quit()
