package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb.ItemAttribute

class BatchPutAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    sdb.createDomain("domaina")
  }

  @Test
  def testPut(): Unit = {
    val as = new java.util.ArrayList[ItemAttribute]()
    as.add(new ItemAttribute("a", "1", true))
    as.add(new ItemAttribute("b", "2", true))
    val bs = new java.util.ArrayList[ItemAttribute]()
    bs.add(new ItemAttribute("c", "3", true))
    val m = new java.util.HashMap[String, java.util.List[ItemAttribute]]()
    m.put("itema", as)
    m.put("itemb", bs)
    domaina.batchPutAttributes(m)
    // Check results
    val itemas = domaina.getItem("itema").getAttributes
    assertEquals(2, itemas.size)
    assertEquals("a", itemas.get(0).getName)
    assertEquals("1", itemas.get(0).getValue)
    assertEquals("b", itemas.get(1).getName)
    assertEquals("2", itemas.get(1).getValue)
    val itembs = domaina.getItem("itemb").getAttributes
    assertEquals(1, itembs.size)
    assertEquals("c", itembs.get(0).getName)
    assertEquals("3", itembs.get(0).getValue)
  }

  @Test
  def testPutTooMany(): Unit = {
    val m = new java.util.HashMap[String, java.util.List[ItemAttribute]]()
    for (i <- 1.to(26)) {
      val as = new java.util.ArrayList[ItemAttribute]()
      as.add(new ItemAttribute("a", "1", true))
      m.put("item" + i, as)
    }
    assertFails("NumberSubmittedItemsExceeded", "Too many items in a single call. Up to 25 items per call allowed.", {
      domaina.batchPutAttributes(m)
    })
  }

}
