
Overview
========

A fake version of Amazon's SimpleDB. Local, in-memory, consistent, deployed as a war.

The entire REST API (Query, Select, etc.) is implemented, though probably with some bugs.

Install
=======

The easiest way is to get the `fakesdb-version.jar` from [github](http://github.com/stephenh/fakesdb/downloads) and run:

* `java -jar fakesdb-version.jar`

This will start up an embedded instance of Jetty on port 8080. You can pass `-Dport=X` to specify a different port.

If you're already running a J2EE container and want to deploy `fakesdb` there, you can download `fakesdb-version.war` also from [github](http://github.com/stephenh/fakesdb/downloads). You'll probably have to mount it in the root context with a virtual host, e.g. `http://fakesdb.mycompany.com`.

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

