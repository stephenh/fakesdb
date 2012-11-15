package fakesdb

import org.junit._
import com.amazonaws.services.simpledb.model._

class BatchPutAttributesTest extends AbstractFakeSdbTest {

  @Before
  def createDomain(): Unit = {
    createDomain("domaina")
  }

  @Test
  def testPut(): Unit = {
    val req = new BatchPutAttributesRequest().withDomainName("domaina").withItems(
      new ReplaceableItem("itema").withAttributes(
        new ReplaceableAttribute("a", "1", true),
        new ReplaceableAttribute("b", "2", true)
      ),
      new ReplaceableItem("itemb").withAttributes(
        new ReplaceableAttribute("c", "3", true)
      )
    )
    sdb.batchPutAttributes(req)

    // Check results
    assertItems("domaina", "itema", "a = 1", "b = 2")
    assertItems("domaina", "itemb", "c = 3")
  }

  @Test
  def testPutTooMany(): Unit = {
    val req = new BatchPutAttributesRequest().withDomainName("domaina")
    for (i <- 1.to(26)) {
      req.withItems(new ReplaceableItem("item"+i).withAttributes(
        new ReplaceableAttribute("a", "1", true)
      ))
    }
    assertFails("NumberSubmittedItemsExceeded", "NumberSubmittedItemsExceeded: Too many items in a single call. Up to 25 items per call allowed.", {
      sdb.batchPutAttributes(req)
    })
  }

}
