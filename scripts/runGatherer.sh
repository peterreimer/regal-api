#!/bin/bash
# dieses Skript stößt einen Lauf der Webgatherer-Sequenz "runGatherer" an.
# Dabei werden alle Websites daraufhin überprüft, ob sie jetzt neu einzusammeln (gathern) sind.
# Falls ja, wird ein Gather-Lauf angestoßen (Übergabe an Heritrix).
# zeitliche Einplaung als cronjob:
#0 20 * * * /opt/regal/cronjobs/runGatherer.sh >> /opt/regal/logs/runGatherer.log
#              
# Änderungshistorie:
# Autor               | Datum      | Beschreibung
# --------------------+------------+-----------------------------------------
# Ingolf Kuss         | 27.05.2016 | Neuanlage auf edoweb-test
# Ingolf Kuss         | 15.01.2018 | Auslagerung von Systemvariablen
# --------------------+------------+-----------------------------------------

# Der Pfad, in dem dieses Skript steht
scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Einlesen der Umgebungsvariablen
cd $scriptdir
source variables.conf
# noch einnmal der Pfad, in dem dieses Skript steht, dieses Mal aus Umgebungsvariablen gebaut:
home_dir=$CRONJOBS_DIR
server=$SERVER
passwd=$REGAL_PASSWORD
project=$PROJECT
regalApi=api.$server

if [ ! -d $REGAL_LOGS ]; then
    mkdir $REGAL_LOGS
fi

echo "Beginn runGatherer"
echo "Aktuelles Datum/Uhrzeit: "`date +"%d.%m.%Y %H:%M:%S"`
echo "home-Verzeichnis: $home_dir"
echo "Projekt: $project"
echo "Server: $server"

runGatherer=`curl -XPOST -u$ADMIN_USER:$passwd "$PROTOCOL://$regalApi/utils/runGatherer"`
echo "$runGatherer\n"; # Ausgabe in Log-Datei

echo "siehe Log-Datei $REGAL_APP/logs/webgatherer.log"
echo "Ende runGatherer am/um"`date +"%d.%m.%Y %H:%M:%S"`
