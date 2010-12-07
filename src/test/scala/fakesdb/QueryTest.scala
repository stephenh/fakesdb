package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb.ItemAttribute

class QueryTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    sdb.createDomain("domaina")
  }

  @Test
  def testEmptyQuery(): Unit = {
    val results = domaina.listItems
    assertEquals(0, results.getItemList.size)
  }

  @Test
  def testEverythingQuery(): Unit = {
    add(domaina, "itema", "a" -> "b")
    add(domaina, "itemb", "a" -> "c")
    val results = domaina.listItems
    assertEquals(2, results.getItemList.size)
    assertEquals("itema", results.getItemList.get(0).getIdentifier)
    assertEquals("itemb", results.getItemList.get(1).getIdentifier)
  }

  @Test
  def testAnd(): Unit = {
    add(domaina, "itema", "a" -> "1")
    add(domaina, "itemb", "a" -> "4")
    val results = domaina.listItems("['a' >= '1' and 'a' <= '3']")
    assertEquals(1, results.getItemList.size)
    assertEquals("itema", results.getItemList.get(0).getIdentifier)
  }

  @Test
  def testSort(): Unit = {
    add(domaina, "itema", "a" -> "2")
    add(domaina, "itemb", "a" -> "1")
    val results = domaina.listItems("['a' > '0'] sort 'a' asc")
    assertEquals(2, results.getItemList.size)
    assertEquals("itemb", results.getItemList.get(0).getIdentifier)
    assertEquals("itema", results.getItemList.get(1).getIdentifier)
  }
}
