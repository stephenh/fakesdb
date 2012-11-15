package fakesdb

import org.junit._
import org.junit.Assert._
import com.amazonaws.services.simpledb.model._

class BatchDeleteAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    createDomain(domaina)
  }

  @Test
  def testDelete(): Unit = {
    add(domaina, "itema", "a" -> "1")
    add(domaina, "itemb", "a" -> "1", "b" -> "1")
    
    sdb.batchDeleteAttributes(new BatchDeleteAttributesRequest().withDomainName(domaina).withItems(
      new DeletableItem().withName("itema"),
      new DeletableItem().withName("itemb")
    ))

    assertEquals(0, select("SELECT * FROM domaina").getItems.size)
  }

  @Test
  def testDeleteTooMany(): Unit = {
    val req = new BatchDeleteAttributesRequest().withDomainName(domaina)
    for (i <- 1.to(26)) {
      req.withItems(new DeletableItem().withName("item" + i))
    }
    assertFails("NumberSubmittedItemsExceeded", "NumberSubmittedItemsExceeded: Too many items in a single call. Up to 25 items per call allowed.", {
      sdb.batchDeleteAttributes(req)
    })
  }

}
