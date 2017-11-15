#! /bin/bash

. variables.conf

pid=$1
server=$2

echo ""
echo "Update Oai Set $pid"
echo ""
curl -s -uedoweb-admin:$PASSWORD -XPOST $server/resource/$pid/oaisets -H"accept: application/json" 
echo""
