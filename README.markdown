
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

* If you're using the `typica` Java SimpleDB client, through 1.6 it only uses port 80, even when given a non-80 setting. So you'll either have to run `fakesdb` on port 80 or else redirect port 80 traffic to 8080 with a firewall rule.

Todo
====

* Convert `build.xml` hack over to `sbt`

