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
      case e => assertEquals("Client error : Invalid domain name domaina", e.getMessage)
    }
  }
}
