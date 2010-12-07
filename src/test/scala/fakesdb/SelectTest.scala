package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb._

class SelectTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    sdb.createDomain("domaina")
  }

  def testCount(): Unit = {
    val results = domaina.selectItems("select count(*) from domaina", null)
    assertEquals(1, results.getItems.size)
    assertEquals("Count", results.getItems.get("Domain").get(0).getName)
    assertEquals("0", results.getItems.get("Domain").get(0).getValue)
  }
}
