#! /bin/bash

scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $scriptdir
source variables.conf

pid=$1
server=$2

echo ""
echo "Update Oai Set $pid"
echo ""
curl -s -u$REGAL_ADMIN:$REGAL_PASSWORD -XPOST $server/resource/$pid/oaisets -H"accept: application/json" 
echo""
