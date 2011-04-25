package fakesdb

import org.junit._
import org.junit.Assert._
import scala.collection.JavaConversions._
import com.amazonaws.services.simpledb.model._

class SelectTest extends AbstractFakeSdbTest {

  @Before
  def createDomain() {
    createDomain(domaina)
  }

  @Test
  def testCount() {
    val results = select("select count(*) from domaina")
    assertEquals(1, results.getItems.size)
    val item = results.getItems.get(0)
    assertEquals("Domain", item.getName)
    assertEquals("Count", item.getAttributes.get(0).getName)
    assertEquals("0", item.getAttributes.get(0).getValue)
    assertEquals(null, results.getNextToken)
  }

  @Test
  def testNextTokenIsNotReturnedIfLimitIsMet() {
    for (i <- 1.to(10)) {
      add(domaina, i.toString(), "a" -> i.toString())
    }
    val results = select("select count(*) from domaina limit 10")
    assertEquals(null, results.getNextToken)
  }

  @Test
  def testPartialSelect() {
    for (i <- 1.to(10)) {
      add(domaina, i.toString(), "a" -> i.toString())
    }
    // http://stackoverflow.com/questions/1795245/how-to-do-paging-with-simpledb/1832779#1832779
    // first query to get a dummy next token
    var results = select("select count(*) from domaina limit 5")
    // second query to start after that
    results = select("select a from domaina limit 5", results.getNextToken)
    assertEquals(5, results.getItems.size)
    var i = 6
    results.getItems foreach { item =>
      item.getAttributes foreach { attr =>
        assertEquals(i.toString(), attr.getValue);
        i += 1
      }
    }
  }
}
