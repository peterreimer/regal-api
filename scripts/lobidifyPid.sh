#! /bin/bash

. variables.conf

pid=$1
server=$2

echo ""
echo "lobidify $pid"
curl -s -uedoweb-admin:$PASSWORD -XPOST $server/utils/lobidify/$pid -H"accept: application/json" 
