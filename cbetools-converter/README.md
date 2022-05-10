# Linked open data tools - Crossroads Bank for Enterprises

Java 11 command line tools to convert CSV dump of the KBO/BCE 
(the official company register in Belgium) to RDF.

Based upon the [open data CSV files](https://kbopub.economie.fgov.be/kbo-open-data/login?lang=en) 
and the [Excel file](http://statbel.fgov.be/nl/statistieken/gegevensinzameling/nomenclaturen/nacebel/) 
published by the FPS Economy.


## Converting full CBE to RDF linked data

* Register (for free) on the CBE website using the [registration form](http://kbopub.economie.fgov.be/kbo-open-data/signup?lang=en&form).   Despite the "\*" on the form next to the company field, having a company number is not required for obtaining a login.

* A confirmation mail containing the username en randomly generated password will 
be sent to your email address.

* Log in on the website using the [login form](http://kbopub.economie.fgov.be/kbo-open-data/login?lang=en).

* Download the ZIP file containing the full export (updated every month) labeled 
`KboOpenData-<sequencenumber>_<year>_<month>`, which is about 200 MB in size. 
Note that this file only contains active companies / organizations.

* Unzip the contents of this ZIP file (several CSV files) in a local directory, 
this requires about 1.5 GB disk space.

* Run the conversion tool with parameters `input-dir export-dir` 
(e.g. `java -jar cbe-converter.jar c:\data\kbo\csv c:\data\kbo\rdf`), 
this may take several minutes, and requires almost 8 GB disk space. 
Make sure your OS / filesystem supports files larger than 4 GB.

* The resulting file (cbe.nt) contains 50+ millions of triples in RDF N-Triples 
format (this is a quite verbose format, but easier / faster to import).

### Generating RDF from monthly update files

* Download the ZIP file containing the monthly updates. 

* Unzip the contents of this ZIP file (several CSV files) in a local directory.

* Run the incremental update conversion tool with parameters `input-dir export-dir` 
(e.g. `java -jar cbe-updater.jar c:\data\kbo\csv c:\data\kbo\rdf`).

## RDF Vocabularies

* [W3C Registered Organization Vocabulary](https://www.w3.org/TR/vocab-regorg/)
* [W3C Organization Vocabulary](https://www.w3.org/TR/vocab-org/)

## Libraries

Uses [OpenCSV](http://opencsv.sourceforge.net/) and Eclipse [RDF4J](http://rdf4j.org/).
