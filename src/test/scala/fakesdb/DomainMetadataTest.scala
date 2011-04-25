package fakesdb

import org.junit._
import org.junit.Assert._
import com.amazonaws.services.simpledb.model._

class DomainMetadataTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    createDomain(domaina)
  }

  @Test
  def testFoo(): Unit = {
    add(domaina, "itema", "aa" -> "111", "bb" -> "222", "bb" -> "333")

    val result = sdb.domainMetadata(new DomainMetadataRequest(domaina))
    assertEquals(1, result.getItemCount)
    assertEquals(5l, result.getItemNamesSizeBytes)
    assertEquals(2, result.getAttributeNameCount)
    assertEquals(4l, result.getAttributeNamesSizeBytes)
    assertEquals(3, result.getAttributeValueCount)
    assertEquals(9l, result.getAttributeValuesSizeBytes)
  }

}
