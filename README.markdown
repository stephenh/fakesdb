
Overview
========

A fake version of SDB. Local, in-memory, consistent, deployed as a war.

The entire REST API (Query, Select, etc.) is implemented, though probably with some bugs.

Install
=======

To get a war, run `sbt package`, via the Scala simple built tool (`sbt`).

Notes
=====

* To facilitate testing, issuing a `CreateDomain` command with `DomainName=_flush` will reset all of the data

* Succinctness was favored, so `src/main` is only ~750 LOC, the bulk being `QueryParser` and `SelectParser`

* The `SelectParser` probably has some bugs in it--the grammar is fairly off-the-cuff. `QueryParser` should be pretty solid.

* [Typica](http://code.google.com/p/typica/) is used for unit testing the server's responses

