# Linked open data tools - Crossroads Bank for Enterprises

Java 8 command line tools to convert CSV dump of the KBO/BCE 
(the official company register in Belgium) to RDF.


Based upon the [open data CSV files](https://kbopub.economie.fgov.be/kbo-open-data/login?lang=en) 
and the [Excel file](http://statbel.fgov.be/nl/statistieken/gegevensinzameling/nomenclaturen/nacebel/) 
published by the FPS Economy.

## CBE as linked data

Examples:

```
http://org.belgif.be/cbe/org/0367_302_178#id  (Fedict)
http://org.belgif.be/cbe/_search?q=fed (Search for names starting with "Fed")
http://org.belgif.be/cbe/_filter?nace=nace2008/84119 (organizations per Nace2008 code)
```

### Content-negotiation

An HTTP client can ask for various  RDF 1.1 serializations, by setting the HTTP `Accept` header.
All requests are HTTP GET request.

  * `application/ld+json`: JSON-LD
  * `text/turtle`: Turtle
  * `application/n-triples`: N-Triples


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
this may take several minutes, and requires almost 5 GB disk space. 
Make sure your OS / filesystem supports files larger than 4 GB.

* The resulting file (cbe.nt) contains 30+ millions of triples in RDF N-Triples 
format (this is a quite verbose format, but easier / faster to import).

### Generating RDF from monthly update files

* Download the ZIP file containing the monthly updates. 

* Unzip the contents of this ZIP file (several CSV files) in a local directory.

* Run the incremental update conversion tool with parameters `input-dir export-dir` 
(e.g. `java -jar cbe-updater.jar c:\data\kbo\csv c:\data\kbo\rdf`).


## Steps to import the RDF into triple store

### Using the LOD-Loader front-end

See instructions on the [LOD Loader](https://github.com/fedict/lod-loader) github.

### Using the OntoText GraphDB 8 workbench:

* Upload the `cbe.nt` file to the  `graphdb-import` directory on the Ontotext 
GraphDB server (when SSH is used to transfer the file, using compression will 
greatly reduce the amount of data / transfer time)

* Go to the OntoText Workbench admin page and create a new namespace (repository).

* Split the cbe.nt file into smaller files (unix split)

* Go to the `Import / RDF / Server files` tab and select the RDF file. 
This may take 15 minutes


## RDF Vocabularies

* [W3C Registered Organization Vocabulary](https://www.w3.org/TR/vocab-regorg/)
* [W3C Organization Vocabulary](https://www.w3.org/TR/vocab-org/)

## Libraries

Uses [OpenCSV](http://opencsv.sourceforge.net/) and Eclipse [RDF4J](http://rdf4j.org/).
