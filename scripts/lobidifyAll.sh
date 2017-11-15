#! /bin/bash

cd /opt/regal/cronjobs

source variables.conf

INDEXNAME=frl

curl -s -XGET localhost:9200/$INDEXNAME/part,issue,journal,monograph,volume,file,webpage/_search -d'{"query":{"match_all":{}},"fields":["/@id"],"size":"50000"}'|egrep -o "$INDEXNAME:[^\"]*" >$ARCHIVE_HOME/logs/pids.txt


curl -s -XGET localhost:9200/$INDEXNAME/journal/_search -d '{"query":{"match_all":{}},"fields":["/@id"],"size":"50000"}'|grep -o "$INDEXNAME:[^\"]*" >$ARCHIVE_HOME/logs/journalPids.txt

curl -s -XGET localhost:9200/$INDEXNAME/journal,monograph,webpage/_search -d'{"query":{"match_all":{}},"fields":["/@id"],"size":"50000"}'|egrep -o "$INDEXNAME:[^\"]*">$ARCHIVE_HOME/logs/titleObjects.txt

#echo "Try to load resources to cache:"
#cat $ARCHIVE_HOME/logs/journalPids.txt | wc -l
#cat $ARCHIVE_HOME/logs/journalPids.txt | parallel curl -s -uedoweb-admin:$PASSWORD -XGET http://localhost:$PLAYPORT/resource/{}/all -H"accept: application/json"  >$ARCHIVE_HOME/logs/initCache-`date +"%Y%m%d"`.log 2>&1

num=`cat $ARCHIVE_HOME/logs/pids.txt|wc -l`
yesterday=`date -d "yesterday 00:00" '+%Y%m%d'`

echo "Update $num resources if changed after $yesterday. Details under $ARCHIVE_HOME/logs/pids.txt." 

echo "lobidify & enrich"
cat $ARCHIVE_HOME/logs/titleObjects.txt | parallel --jobs 5 ./lobidifyPid.sh {} $BACKEND >$ARCHIVE_HOME/logs/lobidify-`date +"%Y%m%d"`.log 2>&1

cd -
