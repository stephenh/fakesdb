package fakesdb

import junit.framework.Assert._
import com.xerox.amazonws.sdb.ItemAttribute

class QueryTest extends AbstractFakeSdbTest {

  override def setUp(): Unit = {
    super.setUp
    sdb.createDomain("domaina")
  }

  def testEmptyQuery(): Unit = {
    val results = domaina.listItems
    assertEquals(0, results.getItemList.size)
  }

  def testEverythingQuery(): Unit = {
    add(domaina, "itema", "a" -> "b")
    add(domaina, "itemb", "a" -> "c")
    val results = domaina.listItems
    assertEquals(2, results.getItemList.size)
    assertEquals("itema", results.getItemList.get(0).getIdentifier)
    assertEquals("itemb", results.getItemList.get(1).getIdentifier)
  }

  def testAnd(): Unit = {
    add(domaina, "itema", "a" -> "1")
    add(domaina, "itemb", "a" -> "4")
    val results = domaina.listItems("['a' >= '1' and 'a' <= '3']")
    assertEquals(1, results.getItemList.size)
    assertEquals("itema", results.getItemList.get(0).getIdentifier)
  }

  def testSort(): Unit = {
    add(domaina, "itema", "a" -> "2")
    add(domaina, "itemb", "a" -> "1")
    val results = domaina.listItems("['a' > '0'] sort 'a' asc")
    assertEquals(2, results.getItemList.size)
    assertEquals("itemb", results.getItemList.get(0).getIdentifier)
    assertEquals("itema", results.getItemList.get(1).getIdentifier)
  }
}
