package fakesdb

import junit.framework.Assert._
import com.xerox.amazonws.sdb._

class SelectTest extends AbstractFakeSdbTest {

  override def setUp(): Unit = {
    super.setUp
    sdb.createDomain("domaina")
  }

  def testCount(): Unit = {
    val results = domaina.selectItems("select count(*) from domaina", null)
    assertEquals(1, results.getItems.size)
    assertEquals("Count", results.getItems.get("domaina").get(0).getName)
    assertEquals("0", results.getItems.get("domaina").get(0).getValue)
  }
}
