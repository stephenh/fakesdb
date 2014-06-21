Overview
========

A fake version of Amazon's SimpleDB. Local, in-memory, consistent, deployed as a war.

The entire REST API (Query, Select, etc.) is implemented in ~750 lines of Scala.

Install
=======

You can get fakesdb from:

1. The [downloads](http://github.com/stephenh/fakesdb/downloads) page

2. The [http://repo.joist.ws](joist.ws) Maven repository, e.g. `com.bizo` + `fakesdb-testing_2.9.1` + `2.6.1` [here](http://repo.joist.ws/com/bizo/fakesdb-testing_2.9.1/)

There are a few different modules:

* `fakesdb-standalone` has all of the dependencies (including Scala) as an uber-jar for running from the CLI.

   You can run this as:

       `java -jar fakesdb-standalone-1.5.jar`

   And it will start up an embedded instance of Jetty on port 8080. You can pass `-Dport=X` to specify a different port.

* `fakesdb-testing` is the dependencies (without Scala) as an uber-jar  for embedding.

   From within a unit test, you can call:

       fakesdb.Jetty.apply(8080)

   To start up fakesdb on port 8080.

* `fakesdb-servlet` is just the fakesdb classes (no dependencies) for running within your own webapp. E.g. add the `fakesdb.FakeSdbServlet` to your own `web.xml` file.

   You could also use this version for `AmazonSimpleDbStub` if you want to do in-memory only testing.

* `fakesdb.war` is (was--it's not being published right now) to drop into your Tomcat/etc.

Notes
=====

* To facilitate testing, issuing a `CreateDomain` command with `DomainName=_flush` will reset all of the data

* If you're using the `typica` Java SimpleDB client, versions through 1.6 only use port 80, even when given a non-80 setting. So you'll either have to run `fakesdb` on port 80 or else redirect port 80 traffic to 8080 with a firewall rule.

Changelog
=========

* 2.6.1 - 15 November 2012
  * Add `<stacktrace>` to error responses (Alex Boisvert)
  * Build improvements (Alex Boisvert)
* 2.5 - 6 Oct 2012
  * Add AmazonSimpleDbStub for in-memory testing of clients using the AWS Java SDK
  * Switch from sbt to buildr
* 2.4 - 23 Aug 2011
  * Upgrade to Scala 2.9.0-1 and Jetty 8.0.0.RC0
  * Make new `fakesdb-testing.jar` which has jetty but not scala-library
* 2.3 - 23 Aug 2011
  * Remove `Jetty` bootstrap classes from `fakesdb-servlet.jar`
* 2.2 - 25 Apr 2011
  * BatchDeleteAttributes support (Alexander Gorkunov)
  * [Partial Select](http://aws.amazon.com/about-aws/whats-new/2009/02/19/new-features-for-amazon-simpledb/) support (Alexander Gorkunov)
* Pre-2.2 various releases

Todo
====

* Loading the SDB URL in a browser (e.g. without a REST action) should display all of the current data
* [Release It](http://www.pragprog.com/titles/mnee/release-it) talks about having "fake" (better term?) versions of systems like `fakesdb` purposefully lock up, fail, etc., to test how your real application responds--it would be cool to flip `fakesdb` into several error modes either via a web UI or meta-domains (like the current `_flush` domain)

## License

Licensed under the terms of the Apache Software License v2.0. 

http://www.apache.org/licenses/LICENSE-2.0.html
