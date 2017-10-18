#! /bin/bash

cd /opt/regal/cronjobs

source variables.conf


dst=`date +"%Y%m%d"`

function trim(){
    echo $1|sed -e 's/^[[:space:]]*//g' -e 's/[[:space:]]*\$//g'
}

function listAllDoisInRepo(){
#   echo "\tList dois in repo - Details at $ARCHIVE_HOME/logs/dois-$dst.csv"
    for i in `cat /opt/regal/logs/titleObjects.txt`
    do 
	id=$i;
	doi=`curl -s localhost:9200/frl/_all/$i |json_pp|grep "doi\"\ :\ \""|sed s/"\"doi\"\ :\ \"\([^\"]*\)\","/"\1"/`;
	doi=`trim $doi`;
	loc=`curl -sI https://dx.doi.org/$doi|grep Location|sed s/"Location:"/""/`;
	echo $id , $doi , $loc
    done > $ARCHIVE_HOME/logs/dois-$dst.csv
    doiInRepo=`cat $ARCHIVE_HOME/logs/dois-$dst.csv|wc -l`
    echo -e "\t$doiInRepo dois in repo"
}

function analyseAllDoisInRepo(){
#   echo -e "\tFind thirdparty dois in repo - Details at $ARCHIVE_HOME/logs/foreignDoi-$dst.csv"
    grep http $ARCHIVE_HOME/logs/dois-$dst.csv| grep -v publisso |grep -v digitool > $ARCHIVE_HOME/logs/foreignDoi-$dst.csv
#   echo -e "\tFind digitool dois in repo - Details at $ARCHIVE_HOME/logs/digitoolDoi-$dst.csv"
    grep digitool $ARCHIVE_HOME/logs/dois-$dst.csv > $ARCHIVE_HOME/logs/digitoolDoi-$dst.csv
#   echo -e "\tFind objects without doi - Details at $ARCHIVE_HOME/logs/noDoi-$dst.csv"
    touch $ARCHIVE_HOME/logs/noDoi-$dst.csv
    grep -v http $ARCHIVE_HOME/logs/dois-$dst.csv > $ARCHIVE_HOME/logs/noDoi-$dst.csv
#   echo -e "\tFind objects without doi variant 2- Details at $ARCHIVE_HOME/logs/doi-noDoi-$dst.csv"
    numOfNoDoi=`curl -s -XGET repository.publisso.de:9200/frl/monograph/_search -d'{"query":{"match_all":{}},"fields":["/@id"], "filter":{"missing":{"field":"doi"}},"size":"50000"}'|json_pp|grep -o "frl:......."|sed s/"\(.*\)"/"https:\/\/repository.publisso.de\/resource\/\1"/|wc -l`
    listOfNoDoi=`curl -s -XGET repository.publisso.de:9200/frl/monograph/_search -d'{"query":{"match_all":{}},"fields":["/@id"], "filter":{"missing":{"field":"doi"}},"size":"50000"}'|json_pp|grep -o "frl:......."|sed s/"\(.*\)"/"https:\/\/repository.publisso.de\/resource\/\1"/`
    echo $listOfNoDoi >> $ARCHIVE_HOME/logs/doi-noDoi-$dst.txt
    thirpartyDoi=`cat $ARCHIVE_HOME/logs/foreignDoi-$dst.csv|wc -l` 
    digitoolDoi=`cat $ARCHIVE_HOME/logs/digitoolDoi-$dst.csv|wc -l`
    noDoi=`cat $ARCHIVE_HOME/logs/noDoi-$dst.csv|wc -l`
    echo -e "\t$thirpartyDoi thirparty dois in repo - Details at $ARCHIVE_HOME/logs/foreignDoi-$dst.csv"
    echo -e "\t$digitoolDoi digitool dois in repo - Details at $ARCHIVE_HOME/logs/digitoolDoi-$dst.csv"
    echo -e "\t$noDoi resource with no doi in repo  - Details at $ARCHIVE_HOME/logs/noDoi-$dst.csv"
    echo -e "\t$numOfNoDoi resource with no doi in index"
   
}

function listAllDoisInDatacite(){
 #  echo -e "\tFind dois in datacite - Details at $ARCHIVE_HOME/logs/datacite-dois-$dst.csv"
    curl -s -uZBMED.ELLINET:EllinetDoi https://mds.datacite.org/doi > $ARCHIVE_HOME/logs/allDataciteDois-$dst.txt
    for i in `cat $ARCHIVE_HOME/logs/allDataciteDois-$dst.txt` 
    do
	doi=$i;
	loc=`curl -sI https://dx.doi.org/$doi|grep Location|sed s/"Location:"/""/ |sed -e 's/^[[:space:]]*//g' -e 's/[[:space:]]*\$//g'`
	id=`echo $loc|sed s/".*\(.......\)$"/"\1"/`
	publisso="https://frl.publisso.de/resource/frl:$id"
	echo $id , $doi , $loc , $publisso
    done > $ARCHIVE_HOME/logs/datacite-dois-$dst.csv
}
function analyseAllDoisInDatacite(){
   dataciteDoi=`cat $ARCHIVE_HOME/logs/datacite-dois-$dst.csv|wc -l`
   titleObjects=`cat $ARCHIVE_HOME/logs/titleObjects.txt|wc -l`
   echo -e "\t$dataciteDoi dois at datacite"
   echo -e "\t$titleObjects title objects in repo" 
#   echo -e "\tFind doi duplicates in datacite - Details at $ARCHIVE_HOME/logs/datacite-occ-$dst.csv"
    grep digitool $ARCHIVE_HOME/logs/datacite-dois-$dst.csv > $ARCHIVE_HOME/logs/datacite-digitoolDoi-$dst.csv
    for i in ` sed 's/\(^.......\).*/\1/' $ARCHIVE_HOME/logs/datacite-dois-$dst.csv `
    do
	occ=`grep -c "$i" $ARCHIVE_HOME/logs/datacite-dois-$dst.csv`
	publisso="https://frl.publisso.de/resource/frl:$i"
	echo $i , $occ , $publisso
    done >  $ARCHIVE_HOME/logs/datacite-occ-$dst.csv
    duplicates=`grep ",\ 2\ ," $ARCHIVE_HOME/logs/datacite-occ-$dst.csv |wc -l`
    echo -e "\t$duplicates duplicates in datacite - Details at $ARCHIVE_HOME/logs/datacite-occ-$dst.csv"
}

echo -e "Run testDoi.sh at $dst" >> $ARCHIVE_HOME/logs/testDoi.log
listAllDoisInRepo  >> $ARCHIVE_HOME/logs/testDoi.log
analyseAllDoisInRepo  >> $ARCHIVE_HOME/logs/testDoi.log
listAllDoisInDatacite  >> $ARCHIVE_HOME/logs/testDoi.log
analyseAllDoisInDatacite  >> $ARCHIVE_HOME/logs/testDoi.log
echo -e "END testDoi.sh $dst" >> $ARCHIVE_HOME/logs/testDoi.log
echo -e "---------------------------" >> $ARCHIVE_HOME/logs/testDoi.log
 
cd -
