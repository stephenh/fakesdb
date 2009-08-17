package fakesdb

import junit.framework.Assert._

class FlushDomainsTest extends AbstractFakeSdbTest {

  def testFoo(): Unit = {
    // Start with a flush
    sdb.createDomain("_flush")

    // Now two real ones
    sdb.createDomain("one")
    sdb.createDomain("two")

    // See that we've got 2
    val domains = sdb.listDomains.getDomainList
    assertEquals(2, domains.size)
    assertEquals("one", domains.get(0).getName)
    assertEquals("two", domains.get(1).getName)

    // Re-flush
    sdb.createDomain("_flush")

    // Now 0
    assertEquals(0, sdb.listDomains.getDomainList.size)
  }

}
