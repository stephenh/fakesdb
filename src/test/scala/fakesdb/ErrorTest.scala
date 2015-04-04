package fakesdb

import org.junit._
import org.junit.Assert._

class ErrorTest extends AbstractFakeSdbTest {
  @Test
  def testEmptyQuery(): Unit = {
    assertFails("InternalError", "RuntimeException: Invalid from domaina", {
      select("SELECT * FROM domaina")
    })
  }
}
