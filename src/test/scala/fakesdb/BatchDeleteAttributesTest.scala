package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb.ItemAttribute

class BatchDeleteAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    sdb.createDomain("domaina")
  }

  @Test
  def testDelete(): Unit = {
    add(domaina, "itema", "a" -> "1")
    add(domaina, "itemb", "a" -> "1", "b" -> "1")

    val as = new java.util.ArrayList[ItemAttribute]()
    val bs = new java.util.ArrayList[ItemAttribute]()
    val m = new java.util.HashMap[String, java.util.List[ItemAttribute]]()
    m.put("itema", as)
    m.put("itemb", bs)
    domaina.batchDeleteAttributes(m)

    assertEquals(0, domaina.selectItems("SELECT * FROM domaina", null, true).getItems.size)
  }

  @Test
  def testDeleteTooMany(): Unit = {
    val m = new java.util.HashMap[String, java.util.List[ItemAttribute]]()
    for (i <- 1.to(26)) {
      val as = new java.util.ArrayList[ItemAttribute]()
      m.put("item" + i, as)
    }
    assertFails("NumberSubmittedItemsExceeded", "Too many items in a single call. Up to 25 items per call allowed.", {
      domaina.batchDeleteAttributes(m)
    })
  }

}
