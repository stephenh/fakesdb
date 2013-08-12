package fakesdb

import org.junit._
import org.junit.Assert._

class ErrorTest extends AbstractFakeSdbTest {
  @Test
  def testEmptyQuery(): Unit = {
    try {
      select("SELECT * FROM domaina")
      fail
    } catch {
      case e: Exception => assertEquals("RuntimeException: Invalid from domaina", e.getMessage)
    }
  }
}
