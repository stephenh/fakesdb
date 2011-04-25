package fakesdb

import org.junit._
import org.junit.Assert._

class FlushDomainsTest extends AbstractFakeSdbTest {

  @Test
  def testFoo(): Unit = {
    // Start with a flush
    createDomain("_flush")

    // Now two real ones
    createDomain("one")
    createDomain("two")

    // See that we've got 2
    val domains = sdb.listDomains.getDomainNames
    assertEquals(2, domains.size)
    assertEquals("one", domains.get(0))
    assertEquals("two", domains.get(1))

    // Re-flush
    createDomain("_flush")

    // Now 0
    assertEquals(0, sdb.listDomains.getDomainNames.size)
  }

}
