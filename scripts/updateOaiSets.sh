#! /bin/bash

scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $scriptdir
source variables.conf

INDEXNAME=frl
TYPE=article,monograph,webpage,journal,part,file

curl -s -XGET localhost:9200/$INDEXNAME/$TYPE/_search -d'{"query":{"match_all":{}},"fields":["/@id"],"size":"5000"}'|egrep -o "$INDEXNAME:[^\"]*">$REGAL_LOGS/${TYPE}Objects.txt

log="$REGAL_LOGS/updateOaiSets-`date +"%Y%m%d"`.log"
echo "Update OAI Sets"
echo "Find logfile in $log"

cat $REGAL_LOGS/${TYPE}Objects.txt | parallel --jobs 5 ./updateOaiSet.sh {} $BACKEND > $log 2>&1

cd -
