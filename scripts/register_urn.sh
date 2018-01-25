#!/bin/bash
# Diese Skript erledigt alternativ drei Aufgaben:
# 1. Kontrolle, ob Meldung an den Katalog und URN-Vergabe für kürzlich angelegte Objekte erfolgt ist.
#    (modus = control)
# 2. Nachregistrierung von URNs für Objekte, bei denen die automatische Registrierung fehlschlug.
#    (modus = register)
# 3. minimales Update auf Objekte, die noch nicht an der Katalogschnittstelle sind
#    (modus = katalog)
# Hintergrund: Alle Objekte sollten 4 Tage nach Neuanlage automatisch registriert werden.
#              Die Häkchen "URN an DNB melden" und "Veröffentlichen über OAI" im Reiter "Extras" werden dabei
#              automatisch gesetzt. Im Reiter Status steht "URN: registriert", nachdem die DNB die Meldung
#              an der OAI-Schnittstelle abgeholt hat.
# Diese Skript guckt bei Objekten, die älter als 4 Tage sind.
# zeitliche Einplaung als cronjobs:
#5 7 * * * /opt/regal/cronjobs/register_urn.sh control  >> /opt/regal/logs/control_urn_vergabe.log
#1 1 * * * /opt/regal/cronjobs/register_urn.sh katalog >> /opt/regal/logs/katalog_update.log
#1 0 * * * /opt/regal/cronjobs/register_urn.sh register >> /opt/regal/logs/register_urn.log
#              
# Änderungshistorie:
# Autor               | Datum      | Beschreibung
# --------------------+------------+-----------------------------------------
# Ingolf Kuss         | 07.12.2015 | Neuanlage als ks.control_urn_vergabe.sh
# Ingolf Kuss         | 14.01.2016 | grep => jq
# Ingolf Kuss         | 22.01.2016 | Neuanlage als ks.register_urn.sh
# Ingolf Kuss         | 19.07.2016 | Neuer Modus "katalog"
# Ingolf Kuss         | 12.01.2018 | Auslagerung von Systemvariablen, 
#                     |            |  Umbenennung nach register_urn.sh
# --------------------+------------+-----------------------------------------

# Der Pfad, in dem dieses Skript steht
scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $scriptdir
source variables.conf
# Parameter
modus="control";
if [ $# -gt 0 ]; then
  if [ "$1" = "r" ] || [ "$1" = "register" ]; then
    modus="register";
  elif [ "$1" = "k" ] || [ "$1" = "katalog" ]; then
    modus="katalog";
  fi
fi
  
# Umgebungsvariablen
# der Pfad, in dem dieses Skript steht:
home_dir=$CRONJOBS_DIR
server=$SERVER
passwd=$REGAL_PASSWORD
project=$PROJECT
regalApi=api.$server
urn_api=api.$server/dnb-urn
oai_id="oai:api.$server:"

if [ ! -d $REGAL_LOGS ]; then
    mkdir $REGAL_LOGS
fi
if [ ! -d $REGAL_TMP ]; then
    mkdir $REGAL_TMP
fi

# bash-Funktionen
function stripOffQuotes {
  local string=$1;
  local len=${#string};
  echo ${string:1:$len-2};
}

# alle neulich erzeugten Objekte durchgehen
# Objekte, die vor sieben bis 21 Tagen angelegt wurden
# Ergebnisliste in eine Datei schreiben; auch eine E-Mail verschicken.
outdatei=$REGAL_TMP/${modus}_urn.$$.out.txt
if [ -f $outdatei ]; then
 rm $outdatei
fi
# E-Mail Inhalt anlegen
mailbodydatei=$REGAL_TMP/mail_$modus.$$.out.txt
if [ -f $mailbodydatei ]; then
 rm $mailbodydatei
fi

if [ "$modus" = "control" ]; then
  echo "Kontrolle von Katalogmeldung und von URN-Vergabe" >> $mailbodydatei
elif [ "$modus" = "register" ]; then
  echo "Folgende Objekte wurden nachregistriert:" >> $mailbodydatei
elif [ "$modus" = "katalog" ]; then
  echo "Nicht erfolgte Katalogmeldungen => minimaler Update" >> $mailbodydatei
fi

aktdate=`date +"%d.%m.%Y"`
echo "Aktuelles Datum: $aktdate" >> $mailbodydatei
echo "home-Verzeichnis: $home_dir" >> $mailbodydatei
echo "Projekt: $project" >> $mailbodydatei
echo "Server: $server" >> $mailbodydatei
typeset -i sekundenseit1970
typeset -i vonsekunden
typeset -i bissekunden
sekundenseit1970=`date +"%s"`
vonsekunden=$sekundenseit1970-1814400; # - 3 Wochen
#vonsekunden=$sekundenseit1970-40000000; # - seit Oktober 2014
bissekunden=$sekundenseit1970-259200; # - 3Tage - vorher: 604800 für 1 Woche
vondatum_hr=`date -d @$vonsekunden +"%Y-%m-%d"`
bisdatum_hr=`date -d @$bissekunden +"%Y-%m-%d"`
echo "Objekte mit Anlagedatum von $vondatum_hr bis $bisdatum_hr:" >> $mailbodydatei
resultset=`curl -s -XGET https://api.$server/search/$project/journal,monograph,file,webpage/_search -d'{"query":{"range" : {"isDescribedBy.created":{"from":"'$vondatum_hr'","to":"'$bisdatum_hr'"}} },"fields":["isDescribedBy.created"],"size":"50000"}'`
#echo "resultset="
#echo $resultset | jq "."
for hit in `echo $resultset | jq -c ".hits.hits[]"`
do
    #echo "hit=";
    #echo $hit | jq "."

    unset id;
    id=`echo $hit | jq "._id"`
    id=$(stripOffQuotes $id)
    #echo "id=$id";

    unset contentType;
    contentType=`echo $hit | jq "._type"`
    contentType=$(stripOffQuotes $contentType)
    #echo "type=$contentType";

    unset cdate;
    for elem in `echo $hit | jq -c ".fields[\"isDescribedBy.created\"][]"`
    do
        cdate=${elem:1:19};
        break;
    done
    #echo "cdate=$cdate";

    if [ -z "$id" ]; then
        continue;
    fi
    if [ -z "$cdate" ]; then
        continue;
    fi

    # >>> Test für eine einzelne ID
    # id="edoweb:7002478"
    # contentType="file"
    # <<< Test

    # Bearbeitung dieser id,cdate
    echo "$aktdate: bearbeite id=$id, Anlagedatum $cdate"; # Ausgabe in log-Datei
    protocol=$PROTOCOL
    url=$protocol://$server/resource/$id
    # Ist das Objekt an der OAI-Schnittstelle "da" ?
    # 1. ist das Objekt an den Katalog gemeldet worden ?
    cat="?";
    if [ "$contentType" = "file" ] || [ "$contentType" = "issue" ] || [ "$contentType" = "volume" ]; then
      cat="X" # Status nicht anwendbar, da Objekt nicht im Katalog verzeichnet wird.
    else
      curlout_kat=$REGAL_TMP/curlout.$$.kat.xml
      curl -s -o $curlout_kat "http://$urn_api/?verb=GetRecord&metadataPrefix=mabxml-1&identifier=$oai_id$id"
      istda_kat=$(grep -c "<identifier>$oai_id$id</identifier>" $curlout_kat);
      if [ $istda_kat -gt 0 ]
      then
        cat="J"
      else
        istnichtda_kat=$(grep -c "<error code=\"idDoesNotExist\">" $curlout_kat);
        if [ $istnichtda_kat ]
        then
         cat="N"
        fi
      fi
      rm $curlout_kat
    fi

    # 2. ist das Objekt an die DNB gemeldet worden (für URN-Vergabe) ?
    if [ "$modus" != "katalog" ]; then
      dnb="?"
      curlout_dnb=$REGAL_TMP/curlout.$$.dnb.xml
      curl -s -o $curlout_dnb "http://$urn_api/?verb=GetRecord&metadataPrefix=epicur&identifier=$oai_id$id"
      istda_dnb=$(grep -c "<identifier>$oai_id$id</identifier>" $curlout_dnb);
      if [ $istda_dnb -gt 0 ]
      then
        dnb="J"
      else
        istnichtda_dnb=$(grep -c "<error code=\"idDoesNotExist\">" $curlout_dnb);
        if [ $istnichtda_dnb ]
        then
          dnb="N"
        fi
      fi
      rm $curlout_dnb
    fi
    
    # >>> Test für eine einzelne ID
    # if [ "$modus" = "register" ]; then
    #   addURN=`curl -XPOST -u$ADMIN_USER:$passwd "https://$regalApi/utils/addUrn?id=${id:7}&namespace=$NAMESPACE&snid=hbz:929:02"`
    #   echo "$addURN\n"; # Ausgabe in log-Datei
    #   addURNresponse=${addURN:0:80}
    #   echo -e "$url\t$cdate\t$cat\t$dnb\t$contentType\t\t$addURNresponse" >> $outdatei
    #   break;
    # fi
    # <<< Test

    if [ "$modus" = "register" ] && [ "$dnb" != "J" ]; then
      # Nachregistrierung des Objektes für URN-Vergabe
      addURN=`curl -XPOST -u$ADMIN_USER:$passwd "https://$regalApi/utils/addUrn?id=${id:7}&namespace=$NAMESPACE&snid=hbz:929:02"`
      echo "$aktdate: $addURN\n"; # Ausgabe in log-Datei
      addURNresponse=${addURN:0:80}
      echo -e "$url\t$cdate\t$cat\t$dnb\t$contentType\t\t$addURNresponse" >> $outdatei
    fi

    if [ "$modus" = "control" ]; then
      echo -e "$url\t$cdate\t$cat\t$dnb\t$contentType" >> $outdatei
    fi

    if [ "$modus" = "katalog" ] && [ "$cat" = "N" ]; then
      # Ausgabe und Weiterbehandlung nur im Fehlerfalle
      # minimalen Update auf das Objekt machen, z.B. über erneutes Setzen der Zugriffrechte
      # dadurch wird das Objekt dann an der Katalogschnittstelle gemeldet
      update=`curl -H "Content-Type: application/json" -XPATCH -u$ADMIN_USER:$passwd -d'{"publishScheme":"public"}' "https://$regalApi/resource/$id"`
      echo "$aktdate: $update\n"; # Ausgabe in log-Datei
      updateResponse=${update:0:80}
      echo -e "$url\t$cdate\t$cat\t$contentType\t\t$updateResponse" >> $outdatei
    fi

    id="";
    cdate="";
done

if [ "$modus" = "control" ]; then
  echo -e "URL\t\t\t\t\t\tAnlagedatum\t\tKatalog\tDNB\tcontentType" >> $mailbodydatei
elif [ "$modus" = "register" ]; then
  echo -e "URL\t\t\t\t\t\tAnlagedatum\t\tKatalog\tDNB\tcontentType\t\"addUrn\"-Response (abbrev. to max 80 chars)" >> $mailbodydatei
elif [ "$modus" = "katalog" ]; then
  echo -e "URL\t\t\t\t\t\tAnlagedatum\t\tKatalog\tcontentType\t\"update\"-Response (abbrev. to max 80 chars)" >> $mailbodydatei
fi
if [ -s $outdatei ]; then
  # outdatei has some data
  outdateisort=$REGAL_TMP/ctrl_urn.$$.out.sort.txt
  sort $outdatei > $outdateisort
  rm $outdatei
  cat $outdateisort >> $mailbodydatei
  rm $outdateisort

  # Versenden des Ergebnisses der Pruefung als E-Mail
  if [ "$modus" = "control" ]; then
    recipients=$EMAIL_RECIPIENT_PROJECT_ADMIN;
  else
    recipients=$EMAIL_RECIPIENT_ADMIN_USERS;
  fi
  subject=" ";
  if [ "$modus" = "control" ]; then
    subject="$project : URN-Vergabe Kontroll-Report";
  elif [ "$modus" = "register" ]; then
    subject="$project : URN-Nachregistrierung";
  elif [ "$modus" = "katalog" ]; then
    subject="$project : WARN: NICHT an KATALOG gemeldete Objekte !!";
  fi
  mailx -s "$subject" $recipients < $mailbodydatei
  # rm $mailbodydatei
fi

cd-
