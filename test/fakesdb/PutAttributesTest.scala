package fakesdb

import junit.framework.Assert._

class PutAttributesTest extends AbstractFakeSdbTest {

  override def setUp(): Unit = {
    super.setUp
    sdb.createDomain("domaina")
  }

  def testPutOne(): Unit = {
    add(domaina, "itema", "a" -> "1")
    val attrs = domaina.getItem("itema").getAttributes
    assertEquals(1, attrs.size)
    assertEquals("a", attrs.get(0).getName)
    assertEquals("1", attrs.get(0).getValue)
  }

  def testPutMultipleValues(): Unit = {
    add(domaina, "itema", "a" -> "1", "a" -> "2")
    val attrs = domaina.getItem("itema").getAttributes
    assertEquals(2, attrs.size)
    assertEquals("a", attrs.get(0).getName)
    assertEquals("1", attrs.get(0).getValue)
    assertEquals("a", attrs.get(1).getName)
    assertEquals("2", attrs.get(1).getValue)
  }

}
