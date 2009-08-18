
A fake version of SDB. Local, in-memory, consistent, deployed as a war.

The entire REST API (Query, Select, etc.) is implemented, though probably with some bugs.

To get a war, run `sbt package`, via the scala simple built tool (sdb).

Easter egg: a CreateDomain command with DomainName=_flush will reset all of the data in anticipation of the next test.

