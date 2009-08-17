package fakesdb

import junit.framework.Assert._

class ListDomainsTest extends AbstractSimpleSdbTest {

  def testFoo(): Unit = {
    sdb.createDomain("domain1")
    sdb.createDomain("domain2")

    val domains = sdb.listDomains.getDomainList
    assertEquals(2, domains.size)
    assertEquals("domain1", domains.get(0).getName)
    assertEquals("domain2", domains.get(1).getName)
  }

}
