#!/bin/sh
CBE="java -jar cbe-updater.jar"
UPD="update$1"

$CBE $UPD/csv $UPD/rdf
