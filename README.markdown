
Overview
========

A fake version of Amazon's SimpleDB. Local, in-memory, consistent, deployed as a war.

The entire REST API (Query, Select, etc.) is implemented in ~750 lines of Scala.

Install
=======

The easiest way is to get the `fakesdb-standalone-1.5.jar` from [github](http://github.com/stephenh/fakesdb/downloads) and run:

* `java -jar fakesdb-standalone-1.5.jar`

This will start up an embedded instance of Jetty on port 8080. You can pass `-Dport=X` to specify a different port.

If you're already running a J2EE container and want to deploy `fakesdb` there, you can download `fakesdb-1.5.war` also from [github](http://github.com/stephenh/fakesdb/downloads).

Notes
=====

* To facilitate testing, issuing a `CreateDomain` command with `DomainName=_flush` will reset all of the data

* The `SelectParser` probably has some bugs in it--the grammar is fairly off-the-cuff. `QueryParser` should be pretty solid.

* [Typica](http://code.google.com/p/typica/) is used for unit testing the server's responses

* If you're using the `typica` Java SimpleDB client, versions through 1.6 only use port 80, even when given a non-80 setting. So you'll either have to run `fakesdb` on port 80 or else redirect port 80 traffic to 8080 with a firewall rule.

Changelog
=========

* 2.2 - 25 Apr 2011
  * BatchDeleteAttributes support (Alexander Gorkunov)
  * [Partial Select](http://aws.amazon.com/about-aws/whats-new/2009/02/19/new-features-for-amazon-simpledb/) support (Alexander Gorkunov)
* Pre-2.2 various releases

Todo
====

* Convert `build.xml` hack over to `sbt`
* Hook up the test suite to `sbt` so that it is scripted
* Loading the SDB URL in a browser (e.g. without a REST action) should display all of the current data
* [Release It](http://www.pragprog.com/titles/mnee/release-it) talks about having "fake" (better term?) versions of systems like `fakesdb` purposefully lock up, fail, etc., to test how your real application responds--it would be cool to flip `fakesdb` into several error modes either via a web UI or meta-domains (like the current `_flush` domain)

