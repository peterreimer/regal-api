#! /bin/bash

cd /opt/regal/cronjobs

source variables.conf

curlog=$ARCHIVE_HOME/logs/lobidify-20160404.log
recipients="edoweb-admin@hbz-nrw.de";
subject="FRL - Fehlerhafte Updates";
mailfile="updateAll.mailbody.txt"
grep "Exception" $curlog|grep -o "frl\:[^\ ]*"|sort -u | sed s,"\(.*\)","https://$SERVER/resource/\1", > $mailfile
mailx -s "$subject" $recipients < $mailfile 
rm $mailfile
