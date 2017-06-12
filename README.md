# Linked open data tools - Crossroads Bank for Enterprises

Java 8 command line tool to convert CSV dump of the KBO/BCE (the official company register in Belgium) to RDF.

## Steps to create an RDF representation of the CBE

* Register (for free) on the CBE website using the [registration form](http://kbopub.economie.fgov.be/kbo-open-data/signup?lang=en&form).   Despite the "\*" on the form next to the company field, having a company number is not required for obtaining a login.

* A confirmation mail containing the username en randomly generated password will be sent to your email address.

* Log in on the website using the [login form](http://kbopub.economie.fgov.be/kbo-open-data/login?lang=en).

* Download the ZIP file containing the full export (updated every month) labeled `KboOpenData-<sequencenumber>_<year>_<month>`, which is about 200 MB in size. Note that this file only contains active companies / organizations.

* Unzip the contents of this ZIP file (several CSV files) in a local directory, this requires about 1.5 GB disk space.

* Run the conversion tool with parameters `input-dir export-dir site-prefix` (e.g. `java -jar cbe-converter.jar c:\data\kbo\csv c:\data\kbo\rdf http://org.belgif.be`), this may take several minutes on a fast PC with an SSD, and requires almost 5 GB disk space. Make sure your OS / filesystem supports files larger than 4 GB.

* The resulting file (cbe.nt) contains 30+ millions of triples in RDF N-Triples format (this is a quite verbose format, but easier / faster to import).

## Steps to import the RDF into GraphDB triple store

* Upload the `cbe.nt` file to the  `graphdb-import` directory on the Ontotext GraphDB server (when SSH is used to transfer the file, using compression will greatly reduce the amount of data / transfer time)

* Go to the OntoText Workbench admin page and create a new namespace (repository).

* Go to the `Import / RDF / Server files` tab and select the RDF file. This may take 15 minutes


## RDF Vocabularies

* [W3C Registered Organization Vocabulary](https://www.w3.org/TR/vocab-regorg/)
* [W3C Organization Vocabulary](https://www.w3.org/TR/vocab-org/)

## Libraries

Uses OpenCSV and Eclipse RDF4J 2.2.1.
