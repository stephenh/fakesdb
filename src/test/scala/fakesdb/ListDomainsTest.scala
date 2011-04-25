package fakesdb

import org.junit._
import org.junit.Assert._

class ListDomainsTest extends AbstractFakeSdbTest {

  @Test
  def testFoo(): Unit = {
    createDomain("domain1")
    createDomain("domain2")

    val domains = sdb.listDomains.getDomainNames
    assertEquals(2, domains.size)
    assertEquals("domain1", domains.get(0))
    assertEquals("domain2", domains.get(1))
  }

}
