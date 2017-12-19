#!python3
#encoding=utf8

import os
import platform
from flask import Flask, request
import sys
import threading
import time
import sys
import tempUmi
import re


app = Flask(__name__)

# variavel que diz se é windows
IS_WINDOWS = platform.platform().find('Windows') != -1

if not IS_WINDOWS:
    from crontab import CronTab


DEBUG = bool(sys.flags.debug or False)
def debug(*args):
    if DEBUG:
        print(*args)


def sendAcCmd(cmd):
    cmd = '/usr/bin/irsend SEND_ONCE LG AC_'+cmd
    print('AcCmd: '+cmd)
    if IS_WINDOWS:
        return
    os.system(cmd)

def error(msg):
    return msg

@app.route('/')
def index():
    return 'index'

@app.route('/ac/temp/<int:temp>')
def setTemp(temp):
    sendAcCmd('LOW_'+str(temp))
    return str(temp)


@app.route('/ac/power/<onOff>')
def setPower(onOff):
    onOff = onOff.upper()
    onOffPossibles = ['ON', 'OFF']
    if onOff not in onOffPossibles:
        return error('Comando "{}" inválido. Digite entre com "on" ou "off".'.format(onOff))

    # caso funcione
    sendAcCmd(onOff)
    return onOff

@app.route('/temp')
def getTemp():
    if IS_WINDOWS:
        return "27.7777"
    return str(tempUmi.getTemp())

@app.route('/umi')
def getUmi():
    if IS_WINDOWS:
        return "33.3333"
    return str(tempUmi.getUmi())


@app.route('/ac/temp/target/<int:temp>')
def setTarget(temp):
    # primeiro, eu defino a temperatura atual
    targetTemp.targetTemp = temp
    targetTemp.currentTemp = temp
    return 'Temperatura ideal definida para '+str(targetTemp.targetTemp)+'ºC'


def selfRequest(url):
    with app.test_client() as c:
        resp = c.get(url)

def Temp(val):
    '''Transforma uma temperatura em string'''
    return str(val)+'C'

class UpdateTemp(threading.Thread):
    def __init__(self):
        super(UpdateTemp, self).__init__()
        # para matar a thread qndo o programa morrer
        self.daemon = True
        # quando o target é None
        # não fico tentando setar a temperatura
        self.targetTemp = None
        # temperatura atual definida pelo controle
        self._currentTemp = None

    @property
    def currentTemp(self):
        return self._currentTemp

    @currentTemp.setter
    def currentTemp(self, value):
        self._currentTemp = value
        setTemp(value)
        print('Temperatura do AC definida automaticamente para: '+Temp(self._currentTemp))
    
    def run(self):
        while True:
            # executa a função de 1m em 1m
            time.sleep(10*60)

            if self.targetTemp is None:
                continue

            # vou definir a temperatura
            temp = round(tempUmi.getTemp())
            print('temp: ', temp)
            print('self.targetTemp: ', self.targetTemp)
            if temp > self.targetTemp:
                if temp > 18:
                    self.currentTemp -= 1
            elif temp < self.targetTemp:
                if temp < 30:
                    self.currentTemp += 1



@app.route('/cronjob/<period>/<int:day>/<int:hour>/<int:minute>/<cmd>')
def setCronjob(period, day, hour, minute, cmd):
    # minuto hora dia mes dia-da-semana linha-de-comando
    # vamos definir a cron até a linha de comando
    cronStart = str(minute)+' '+str(hour)
    if period == 'month':
        cronTime = cronStart+' '+str(day)+' * *'
    elif period == 'week':
        cronTime = cronStart+' * * '+str(day)
    elif period == 'day':
        cronTime = cronStart+' * * *'
    else:
        return 'Erro'
    cmd = 'sudo python3 /home/pi/shared/ac.py '+cmd
    cronLine = cronTime+' '+cmd

    if not IS_WINDOWS:
        my_cron = CronTab(user='pi')
        job = my_cron.new(command=cmd)
        job.setall(cronTime)
        my_cron.write()

    return cronLine


@app.route('/cronjob/get/')
def getCronjob():
    res = []
    cron = CronTab(user='pi')
    # cron = ['0 15 * * 0 sudo python3 /home/pi/shared/ac.py 22', '0 15 * * 0 sudo python3 /home/pi/shared/ac.py 22']
    for c in cron:
        if c.is_enabled():
            c = str(c)
            termos = re.findall(r'(\S+)', c)
            # retonar: dia-semana hora:minuto 22

            # vamos trocar o termo 4 que é o dia da semana
            semanaDia = ['domingo', 'segunda', 'terça', 'quarta', 'quinta', 'sexta', 'sábado']
            if termos[4] != '*':
                termos[4] = semanaDia[int(termos[4])]
            else:
                termos[4] = 'todos os dias da semana'

            # coloca um 0 a mais no minuto
            minutos = termos[0]
            if len(minutos) == 1:
                minutos = '0'+minutos
            res.append(termos[4]+' '+termos[1]+':'+minutos+' '+termos[-1])
    return '\n'.join(res)


@app.route('/cronjob/remove/<int:index>/')
def removeCronjob(index):
    cron = CronTab(user='pi')
    i = 0
    for c in cron:
        if c.is_enabled():
            if i == index:
                c.delete()
                cron.write()
                return '1'
            i+=1
    return '0'



if __name__ == '__main__':
    try:
        if len(sys.argv) > 1:
            if re.match(r'\d+', sys.argv[1]):
                setPower('on') 
                time.sleep(10)
                setTemp(int(sys.argv[1]))
            elif sys.argv[1].upper() == 'OFF':
                setPower('off') 
            sys.exit(0)


        global targetTemp
        targetTemp = UpdateTemp()
        targetTemp.start()

        app.run(host='0.0.0.0', debug=True)
    except KeyboardInterrupt as e:
        pass
    except Exception as e:
        raise e