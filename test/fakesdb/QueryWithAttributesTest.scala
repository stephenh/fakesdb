package fakesdb

import junit.framework.Assert._
import com.xerox.amazonws.sdb.ItemAttribute

class QueryWithAttributesTest extends AbstractSimpleSdbTest {

  override def setUp(): Unit = {
    super.setUp
    sdb.createDomain("domaina")
  }

  def testEmpty(): Unit = {
    val results = domaina.listItemsWithAttributes(null, null, null, 0)
    assertEquals(0, results.getItems.size)
  }

  def testEverything(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    add(domaina, "itemb", "a" -> "3")
    val results = domaina.listItemsWithAttributes(null, null, null, 0)
    assertEquals(2, results.getItems.size)
    assertEquals("a", results.getItems.get("itema").get(0).getName)
    assertEquals("1", results.getItems.get("itema").get(0).getValue)
    assertEquals("b", results.getItems.get("itema").get(1).getName)
    assertEquals("2", results.getItems.get("itema").get(1).getValue)
    assertEquals("a", results.getItems.get("itemb").get(0).getName)
    assertEquals("3", results.getItems.get("itemb").get(0).getValue)
  }

  def testQueryOneWithAllAttributes(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    add(domaina, "itemb", "a" -> "3")
    val results = domaina.listItemsWithAttributes("['a' = '1']", null, null, 0)
    assertEquals(1, results.getItems.size)
    assertEquals("a", results.getItems.get("itema").get(0).getName)
    assertEquals("1", results.getItems.get("itema").get(0).getValue)
    assertEquals("b", results.getItems.get("itema").get(1).getName)
    assertEquals("2", results.getItems.get("itema").get(1).getValue)
  }

  def testQueryOneWithSomeAttributes(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    add(domaina, "itemb", "a" -> "3")
    val attrs = new java.util.ArrayList[String]()
    attrs.add("b")
    val results = domaina.listItemsWithAttributes("['a' = '1']", attrs, null, 0)
    assertEquals(1, results.getItems.size)
    assertEquals(1, results.getItems.get("itema").size) // only b was returned
    assertEquals("b", results.getItems.get("itema").get(0).getName)
    assertEquals("2", results.getItems.get("itema").get(0).getValue)
  }

  def testQueryOneWithNoneOfTheAttributes(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    add(domaina, "itemb", "a" -> "1") // both meet the where clause
    val attrs = new java.util.ArrayList[String]()
    attrs.add("b")
    val results = domaina.listItemsWithAttributes("['a' = '1']", attrs, null, 0)
    assertEquals(1, results.getItems.size)
    assertEquals(1, results.getItems.get("itema").size) // only b was returned
    assertEquals("b", results.getItems.get("itema").get(0).getName)
    assertEquals("2", results.getItems.get("itema").get(0).getValue)
  }

  def testSort(): Unit = {
    add(domaina, "itema", "a" -> "2")
    add(domaina, "itemb", "a" -> "1")
    val results = domaina.listItemsWithAttributes("['a' > '0'] sort 'a' asc", null, null, 0)
    assertEquals(2, results.getItems.size)
    assertEquals("a", results.getItems.get("itemb").get(0).getName)
    assertEquals("1", results.getItems.get("itemb").get(0).getValue)
    assertEquals("a", results.getItems.get("itema").get(0).getName)
    assertEquals("2", results.getItems.get("itema").get(0).getValue)
  }

}
