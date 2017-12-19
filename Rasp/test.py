#python3
#encoding=utf-8

import RPi.GPIO as gpio
import time

gpio.setmode(gpio.BCM)

gpio.setup(4, gpio.OUT)

tempo = 1E-10
tempo /= 2

try:
    while True:
        # time.sleep(tempo)
        gpio.output(4, gpio.HIGH)
        # time.sleep(tempo)
        gpio.output(4, gpio.LOW)
except KeyboardInterrupt as e:
    pass
except Exception as e:
    raise e
finally:
    gpio.cleanup()