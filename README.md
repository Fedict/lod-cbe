# Linked open data tools - Crossroads Bank for Enterprises

Java 8 command line tool to convert CSV dump of the KBO/BCE (the official company register in Belgium) to RDF.

The ZIP with the CSV files can be obtained from the [KBO open data registration page](https://kbopub.economie.fgov.be/kbo-open-data/?lang=en)

The result is a fairly large RDF (N-Triples) file: currently the conversion produces a 4 GB file, containing about 30 million triples.
Compression this file with ZIP or HDT reduces the file size to 200-250 MB.

## RDF Vocabularies

* [W3C Registered Organization Vocabulary](https://www.w3.org/TR/vocab-regorg/)
* [W3C Organization Vocabulary](https://www.w3.org/TR/vocab-org/)

## Libraries

Uses OpenCSV and Eclipse RDF4J 2.1 snapshot
