import platform

IS_WINDOWS = platform.platform().find('Windows') != -1

if not IS_WINDOWS:
    from pi_sht1x import SHT1x
    import RPi.GPIO as GPIO

def getTemp():
    if IS_WINDOWS:
        return 27.777
    with SHT1x(18, 23, gpio_mode=GPIO.BCM) as sensor:
        return sensor.read_temperature()

def getUmi():
    if IS_WINDOWS:
        return 35.555
    with SHT1x(18, 23, gpio_mode=GPIO.BCM) as sensor:
        return sensor.read_humidity()

def cleanup():
    if IS_WINDOWS:
        return ;
    GPIO.cleanup()