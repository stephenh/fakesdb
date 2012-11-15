package fakesdb

import org.junit._
import org.junit.Assert._
import com.amazonaws.services.simpledb.model._
import com.amazonaws.services.simpledb.model.{Attribute => AwsAttribute}
import scala.collection.JavaConversions._

class DeleteAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    createDomain("domaina")
  }

  @Test
  def testDeleteOneEntireAttribute(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    assertItems(domaina, "itema", "a = 1", "b = 2")

    delete("a")
    assertItems(domaina, "itema", "b = 2")
  }

  @Test
  def testDeleteBothAttributesDeletesTheItem(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete("a", "b")
    assertEquals(0, select("SELECT * FROM domaina").getItems.size)
  }

  @Test
  def testDeleteNoAttributesDeletesTheItem(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete()
    assertEquals(0, select("SELECT * FROM domaina").getItems.size)
  }

  @Test
  def testDeleteConditionalValueSuccess(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete(hasValue("a", "1"), "a")
    assertHas("b = 2")
  }

  @Test
  def testDeleteConditionalDoesNotExistSuccess(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    delete(doesNotExist("c"), "a")
    assertHas("b = 2")
  }

  @Test
  def testDeleteConditionalValueFailure(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    assertFails("ConditionalCheckFailed", "ConditionalCheckFailedException: Attribute (a) value is (List(1)) but was expected (2)", {
      delete(hasValue("a", "2"), "a")
    })
    assertHas("a = 1", "b = 2")
  }

  @Test
  def testDeleteConditionalDoesNotExistFailure(): Unit = {
    add(domaina, "itema", "a" -> "1", "b" -> "2")
    assertFails("ConditionalCheckFailed", "ConditionalCheckFailedException: Attribute (b) value exists", {
      delete(doesNotExist("b"), "a")
    })
    assertHas("a = 1", "b = 2")
  }

  private def assertHas(attrs: String*): Unit = {
    assertItems(domaina, "itema", attrs: _*)
  }

  private def delete(attrNames: String*): Unit = {
    sdb.deleteAttributes(new DeleteAttributesRequest()
      .withDomainName(domaina)
      .withItemName("itema")
      .withAttributes(attrNames.map { new AwsAttribute(_, null) }))
  }

  private def delete(cond: UpdateCondition, attrNames: String*): Unit = {
    sdb.deleteAttributes(new DeleteAttributesRequest()
      .withDomainName(domaina)
      .withItemName("itema")
      .withAttributes(attrNames.map { new AwsAttribute(_, null) })
      .withExpected(cond))
  }

}
