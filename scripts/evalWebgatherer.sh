#!/bin/bash
# Auswertung von Log-File webgatherer.log
#| Autor              | Datum      | Beschreibung
#+--------------------+------------+-----------------------------------------
#| I. Kuss            | 02.06.2016 | Neuerstellung
#| I. Kuss            | 12.01.2018 | Auslagerung von Systemvariablen
#+--------------------+------------+-----------------------------------------
#
# Der Pfad, in dem dieses Skript steht
scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $scriptdir
source variables.conf
echo "AUSWERTUNG WEBGATHERER.log : "
LOG=$REGAL_APP/logs/webgatherer.log
echo "Anzahl gefundener   Sites: " `cat $LOG | grep -o "Found .* webpages"`
echo "Anzahl berabeiteter Sites: " `cat $LOG | grep -c "Precount: "`
echo "Jetzt neu gestartete Crawls: Anzahl:" `cat $LOG | grep -c "Create new version for:.*"`
echo "Neu gestartete Crawls mit ID:"
cat $LOG | grep -o "Create new version for:.*"
echo "Dabei geschmissene Fehler: Anzahl:" `cat $LOG | grep -c "ERROR"`
cat $LOG | grep "ERROR"
# echo "Demnächst anstehende Gatherläufe: Anzahl:" `cat $LOG | grep -c "will be launched next time at"`
# echo "Anstehende sortiert nach Zeitpunkt:"
# cat $LOG | grep -o "will be launched next time at.*" | sort
echo "ENDE AUSWERTUNG WEBGATHERER.log"
