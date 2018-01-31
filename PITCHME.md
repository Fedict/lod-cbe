# CBE converter

> Converting company register to LOD

---

## KBO Open Data

- Full and incremental updates available
  - Updated every month
  - (free) registration required
- Only contains active organizations

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