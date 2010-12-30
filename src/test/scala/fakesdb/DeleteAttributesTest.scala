package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb.Condition
import com.xerox.amazonws.sdb.ItemAttribute
import scala.collection.JavaConversions._

class DeleteAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    sdb.createDomain("domaina")
  }

  @Test
  def testDeleteOneEntireAttribute(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")

    val itema = domaina.getItem("itema")
    assertEquals(2, itema.getAttributes.size)

    delete("a")

    val now = itema.getAttributes
    assertEquals(1, now.size)
    assertEquals("b", now.get(0).getName)
  }

  @Test
  def testDeleteBothAttributesDeletesTheItem(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete("a", "b")
    assertEquals(0, domaina.selectItems("SELECT * FROM domaina", null, true).getItems.size)
  }

  @Test
  def testDeleteNoAttributesDeletesTheItem(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete()
    assertEquals(0, domaina.selectItems("SELECT * FROM domaina", null, true).getItems.size)
  }

  @Test
  def testDeleteConditionalValueSuccess(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete(hasValue("a", "1"), "a")
    assertHas("b" -> "2")
  }

  @Test
  def testDeleteConditionalDoesNotExistSuccess(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete(doesNotExist("c"), "a")
    assertHas("b" -> "2")
  }

  @Test
  def testDeleteConditionalValueFailure(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    assertFails("ConditionalCheckFailed", "Attribute (a) value is (List(1)) but was expected (2)", {
      delete(hasValue("a", "2"), "a")
    })
    assertHas("a" -> "1", "b" -> "2")
  }

  @Test
  def testDeleteConditionalDoesNotExistFailure(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    assertFails("ConditionalCheckFailed", "Attribute (b) value exists", {
      delete(doesNotExist("b"), "a")
    })
    assertHas("a" -> "1", "b" -> "2")
  }

  private def assertHas(attrs: KV*): Unit = {
    val now = domaina.getItem("itema").getAttributes
    assertEquals(attrs.size, now.size)
    for (i <- 0.until(attrs.size)) {
      assertEquals(attrs.get(i)._1, now.get(i).getName)
      assertEquals(attrs.get(i)._2, now.get(i).getValue)
    }
  }

  private def delete(attrNames: String*): Unit = {
    domaina getItem("itema") deleteAttributes(attrNames.map { new ItemAttribute(_, null, false) })
  }

  private def delete(cond: Condition, attrNames: String*): Unit = {
    domaina getItem("itema") deleteAttributes(attrNames.map { new ItemAttribute(_, null, false) }, List(cond))
  }

}
