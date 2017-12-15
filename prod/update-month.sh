#!/bin/bash
ZIP="zip -q -9 -j"
UPD="update$1"
FILE="update$1.zip"
CURL="curl -X POST --basic -u CHANGE:ME"
HEADER="Content-Type: application/zip"
HOST="https://CHANGE.ME/_upload/load/organizations" 

mv $UPD/rdf/cbe-upd.nt $UPD/rdf/zz-upd.nt
$ZIP $FILE $UPD/rdf/*.csv $UPD/rdf/*.nt
$CURL $HOST/$FILE -H "$HEADER" --data-binary @$FILE

