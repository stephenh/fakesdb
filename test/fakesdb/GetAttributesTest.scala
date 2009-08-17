package fakesdb

import junit.framework.Assert._

class GetAttributesTest extends AbstractSimpleSdbTest {

  override def setUp(): Unit = {
    super.setUp
    sdb.createDomain("domaina")
  }

  def testGetMultipleValues(): Unit = {
    add(domaina, "itema", "a" -> "1", "a" -> "2", "b" -> "3")
    val attrs = domaina.getItem("itema").getAttributes
    assertEquals(3, attrs.size)
    assertEquals("a", attrs.get(0).getName)
    assertEquals("1", attrs.get(0).getValue)
    assertEquals("a", attrs.get(1).getName)
    assertEquals("2", attrs.get(1).getValue)
    assertEquals("b", attrs.get(2).getName)
    assertEquals("3", attrs.get(2).getValue)
  }

  def testGetOneAttribute(): Unit = {
    add(domaina, "itema",
        "a" -> "1",
        "b" -> "2")

    val get = new java.util.ArrayList[String]()
    get.add("a")

    val attrs = domaina.getItem("itema").getAttributes(get)
    assertEquals(1, attrs.size)
    assertEquals("a", attrs.get(0).getName)
    assertEquals("1", attrs.get(0).getValue)
  }

  def testGetAttributesDoesNotCreateAnItem(): Unit = {
    val attrs = domaina.getItem("itema").getAttributes
    assertEquals(0, attrs.size)
    assertEquals(0, domaina.listItems.getItemList.size)
  }
}
