package fakesdb

import org.junit._
import org.junit.Assert._
import com.amazonaws.services.simpledb.model._

class GetAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    createDomain(domaina)
  }

  @Test
  def testGetMultipleValues(): Unit = {
    add(domaina, "itema", "a" -> "1", "a" -> "2", "b" -> "3")
    val attrs = sdb.getAttributes(new GetAttributesRequest(domaina, "itema")).getAttributes
    assertEquals(3, attrs.size)
    assertEquals("a", attrs.get(0).getName)
    assertEquals("1", attrs.get(0).getValue)
    assertEquals("a", attrs.get(1).getName)
    assertEquals("2", attrs.get(1).getValue)
    assertEquals("b", attrs.get(2).getName)
    assertEquals("3", attrs.get(2).getValue)
  }

  @Test
  def testGetOneAttribute(): Unit = {
    add(domaina, "itema",
        "a" -> "1",
        "b" -> "2")

    val attrs = sdb.getAttributes(new GetAttributesRequest(domaina, "itema").withAttributeNames("a")).getAttributes
    assertEquals(1, attrs.size)
    assertEquals("a", attrs.get(0).getName)
    assertEquals("1", attrs.get(0).getValue)
  }

  @Test
  def testGetAttributesDoesNotCreateAnItem(): Unit = {
    val attrs = sdb.getAttributes(new GetAttributesRequest(domaina, "itema")).getAttributes
    assertEquals(0, attrs.size)
    assertEquals(0, select("SELECT * FROM domaina").getItems.size)
  }
}
