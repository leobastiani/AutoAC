from crontab import CronTab
 
my_cron = CronTab(user='pi')
#job = my_cron.new(command='sudo python3 /home/pi/shared/job.py')
job = my_cron.new(command='pwd > /home/pi/shared/text.txt')
job.minute.every(1)

#job.setall('49 20 * * *')
 
my_cron.write()