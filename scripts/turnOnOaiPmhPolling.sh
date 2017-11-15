#! /bin/bash

cd /opt/regal/cronjobs
source variables.conf

mysql -uproai -p$PASSWORD -e"UPDATE proai.rcAdmin SET pollingEnabled=1;"

cd -
