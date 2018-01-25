#! /bin/bash

scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $scriptdir
source variables.conf

dst=`date +"%Y%m%d"`

function trim(){
    echo $1|sed -e 's/^[[:space:]]*//g' -e 's/[[:space:]]*\$//g'
}

function listAllDoisInRepo(){
#   echo "\tList dois in repo - Details at $REGAL_LOGS/dois-$dst.csv"
    for i in `cat $REGAL_LOGS/titleObjects.txt`
    do 
	id=$i;
	doi=`curl -s localhost:9200/frl/_all/$i |json_pp|grep "doi\"\ :\ \""|sed s/"\"doi\"\ :\ \"\([^\"]*\)\","/"\1"/`;
	doi=`trim $doi`;
	loc=`curl -sI https://dx.doi.org/$doi|grep Location|sed s/"Location:"/""/`;
	echo $id , $doi , $loc
    done > $REGAL_LOGS/dois-$dst.csv
    doiInRepo=`cat $REGAL_LOGS/dois-$dst.csv|wc -l`
    echo -e "\t$doiInRepo dois in repo"
}

function analyseAllDoisInRepo(){
#   echo -e "\tFind thirdparty dois in repo - Details at $REGAL_LOGS/foreignDoi-$dst.csv"
    grep http $REGAL_LOGS/dois-$dst.csv| grep -v publisso |grep -v digitool > $REGAL_LOGS/foreignDoi-$dst.csv
#   echo -e "\tFind digitool dois in repo - Details at $REGAL_LOGS/digitoolDoi-$dst.csv"
    grep digitool $REGAL_LOGS/dois-$dst.csv > $REGAL_LOGS/digitoolDoi-$dst.csv
#   echo -e "\tFind objects without doi - Details at $REGAL_LOGS/noDoi-$dst.csv"
    touch $REGAL_LOGS/noDoi-$dst.csv
    grep -v http $REGAL_LOGS/dois-$dst.csv > $REGAL_LOGS/noDoi-$dst.csv
#   echo -e "\tFind objects without doi variant 2- Details at $REGAL_LOGS/doi-noDoi-$dst.csv"
    numOfNoDoi=`curl -s -XGET repository.publisso.de:9200/frl/monograph/_search -d'{"query":{"match_all":{}},"fields":["/@id"], "filter":{"missing":{"field":"doi"}},"size":"50000"}'|json_pp|grep -o "frl:......."|sed s/"\(.*\)"/"https:\/\/repository.publisso.de\/resource\/\1"/|wc -l`
    listOfNoDoi=`curl -s -XGET repository.publisso.de:9200/frl/monograph/_search -d'{"query":{"match_all":{}},"fields":["/@id"], "filter":{"missing":{"field":"doi"}},"size":"50000"}'|json_pp|grep -o "frl:......."|sed s/"\(.*\)"/"https:\/\/repository.publisso.de\/resource\/\1"/`
    echo $listOfNoDoi >> $REGAL_LOGS/doi-noDoi-$dst.txt
    thirpartyDoi=`cat $REGAL_LOGS/foreignDoi-$dst.csv|wc -l` 
    digitoolDoi=`cat $REGAL_LOGS/digitoolDoi-$dst.csv|wc -l`
    noDoi=`cat $REGAL_LOGS/noDoi-$dst.csv|wc -l`
    echo -e "\t$thirpartyDoi thirparty dois in repo - Details at $REGAL_LOGS/foreignDoi-$dst.csv"
    echo -e "\t$digitoolDoi digitool dois in repo - Details at $REGAL_LOGS/digitoolDoi-$dst.csv"
    echo -e "\t$noDoi resource with no doi in repo  - Details at $REGAL_LOGS/noDoi-$dst.csv"
    echo -e "\t$numOfNoDoi resource with no doi in index"
   
}

function listAllDoisInDatacite(){
 #  echo -e "\tFind dois in datacite - Details at $REGAL_LOGS/datacite-dois-$dst.csv"
    curl -s -uZBMED.ELLINET:EllinetDoi https://mds.datacite.org/doi > $REGAL_LOGS/allDataciteDois-$dst.txt
    for i in `cat $REGAL_LOGS/allDataciteDois-$dst.txt` 
    do
	doi=$i;
	loc=`curl -sI https://dx.doi.org/$doi|grep Location|sed s/"Location:"/""/ |sed -e 's/^[[:space:]]*//g' -e 's/[[:space:]]*\$//g'`
	id=`echo $loc|sed s/".*\(.......\)$"/"\1"/`
	publisso="https://frl.publisso.de/resource/frl:$id"
	echo $id , $doi , $loc , $publisso
    done > $REGAL_LOGS/datacite-dois-$dst.csv
}
function analyseAllDoisInDatacite(){
   dataciteDoi=`cat $REGAL_LOGS/datacite-dois-$dst.csv|wc -l`
   titleObjects=`cat $REGAL_LOGS/titleObjects.txt|wc -l`
   echo -e "\t$dataciteDoi dois at datacite"
   echo -e "\t$titleObjects title objects in repo" 
#   echo -e "\tFind doi duplicates in datacite - Details at $REGAL_LOGS/datacite-occ-$dst.csv"
    grep digitool $REGAL_LOGS/datacite-dois-$dst.csv > $REGAL_LOGS/datacite-digitoolDoi-$dst.csv
    for i in ` sed 's/\(^.......\).*/\1/' $REGAL_LOGS/datacite-dois-$dst.csv `
    do
	occ=`grep -c "$i" $REGAL_LOGS/datacite-dois-$dst.csv`
	publisso="https://frl.publisso.de/resource/frl:$i"
	echo $i , $occ , $publisso
    done >  $REGAL_LOGS/datacite-occ-$dst.csv
    duplicates=`grep ",\ 2\ ," $REGAL_LOGS/datacite-occ-$dst.csv |wc -l`
    echo -e "\t$duplicates duplicates in datacite - Details at $REGAL_LOGS/datacite-occ-$dst.csv"
}

echo -e "Run testDoi.sh at $dst" >> $REGAL_LOGS/testDoi.log
listAllDoisInRepo  >> $REGAL_LOGS/testDoi.log
analyseAllDoisInRepo  >> $REGAL_LOGS/testDoi.log
listAllDoisInDatacite  >> $REGAL_LOGS/testDoi.log
analyseAllDoisInDatacite  >> $REGAL_LOGS/testDoi.log
echo -e "END testDoi.sh $dst" >> $REGAL_LOGS/testDoi.log
echo -e "---------------------------" >> $REGAL_LOGS/testDoi.log
 
cd -
