package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb._
import scala.collection.JavaConversions._

class SelectTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    sdb.createDomain("domaina")
  }

  @Test
  def testCount(): Unit = {
    val results = domaina.selectItems("select count(*) from domaina", null)
    assertEquals(1, results.getItems.size)
    assertEquals("Count", results.getItems.get("Domain").get(0).getName)
    assertEquals("0", results.getItems.get("Domain").get(0).getValue)
  }

  @Test
  def testPartialSelect(): Unit = {
    for (i <- 1.to(10)) {
      add(domaina, i.toString(), "a" -> i.toString())
    }
    var results = domaina.selectItems("select count(*) from domaina limit 5", null)
    results = domaina.selectItems("select a from domaina limit 5", results.getRequestId)
    assertEquals(5, results.getItems.size)
    var i = 6
    results.getItems foreach { 
      case(key, attrs) => assertEquals(i.toString(), attrs.get(0).getValue); i += 1
    }
  }
}
