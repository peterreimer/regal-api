#! /bin/bash

cd /opt/regal/cronjobs

source variables.conf

INDEXNAME=frl
TYPE=article,monograph,webpage,journal,part,file

curl -s -XGET localhost:9200/$INDEXNAME/$TYPE/_search -d'{"query":{"match_all":{}},"fields":["/@id"],"size":"5000"}'|egrep -o "$INDEXNAME:[^\"]*">$ARCHIVE_HOME/logs/${TYPE}Objects.txt

log="$ARCHIVE_HOME/logs/updateOaiSets-`date +"%Y%m%d"`.log"
echo "Update OAI Sets"
echo "Find logfile in $log"

cat $ARCHIVE_HOME/logs/${TYPE}Objects.txt | parallel --jobs 5 ./updateOaiSet.sh {} $BACKEND > $log 2>&1

cd -
