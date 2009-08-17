package fakesdb

import junit.framework.Assert._
import com.xerox.amazonws.sdb.ItemAttribute

class DeleteAttributesTest extends AbstractSimpleSdbTest {

  override def setUp(): Unit = {
    flush()
    sdb.createDomain("domaina")
  }

  def testDeleteOneEntireAttribute(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")

    val itema = domaina.getItem("itema")
    assertEquals(2, itema.getAttributes.size)

    val toDelete = new java.util.ArrayList[ItemAttribute]()
    toDelete.add(new ItemAttribute("a", null, false))
    itema.deleteAttributes(toDelete)

    val now = itema.getAttributes
    assertEquals(1, now.size)
    assertEquals("b", now.get(0).getName)
  }

  def testDeleteBothAttributesDeletesTheItem(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")

    val itema = domaina.getItem("itema")
    assertEquals(2, itema.getAttributes.size)

    val toDelete = new java.util.ArrayList[ItemAttribute]()
    toDelete.add(new ItemAttribute("a", null, false))
    toDelete.add(new ItemAttribute("b", null, false))
    itema.deleteAttributes(toDelete)

    assertEquals(0, domaina.listItems.getItemList.size)
  }

  def testDeleteNoAttributesDeletesTheItem(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")

    val itema = domaina.getItem("itema")
    assertEquals(2, itema.getAttributes.size)

    val toDelete = new java.util.ArrayList[ItemAttribute]()
    itema.deleteAttributes(toDelete)

    assertEquals(0, domaina.listItems.getItemList.size)
  }

}
