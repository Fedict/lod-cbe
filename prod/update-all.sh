#!/bin/bash
OUT=out
ZIP="zip -q -9 -j"
CURL="curl -X POST --basic -u CHANGE:ME"
HEADER="Content-Type: application/zip"
HOST="https://CHANG.ME/_upload/load/organizations" 


ARR=$(ls full/*.zip)

for item in ${ARR[*]}
do
	F=${item/full\//}
	echo $F
	$CURL $HOST/$F -H "$HEADER" --data-binary @${item}
	sleep 30 
done

