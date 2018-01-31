# CBE converter

> Converting company register to LOD

---

## KBO Open Data

- Full and incremental updates available
  - (free) registration required
- Only contains active organizations

---

## Features

- Uses W3C [Registered Organization](https://www.w3.org/TR/vocab-regorg/)

---

## How it works

- KBO publishes monthly updates
- Tool converts CSV to RDF files
- Zipped files sent to upload tool
- Data is loaded into OntoText GraphDB

---

## Technology

- Java open source
    - [RDF4J](http://rdf4j.org/), [OpenCSV](http://opencsv.sourceforge.net/)
- Small bash scripts and cron job

---

## Thank you

Questions ? 

@fa[twitter] @BartHanssens

@fa[envelope] [opendata@belgium.be](mailto:opendata@belgium.be)