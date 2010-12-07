package fakesdb

import org.junit.Assert._
import com.xerox.amazonws.sdb.ItemAttribute
import com.xerox.amazonws.sdb.SimpleDB

class ErrorTest extends AbstractFakeSdbTest {
  def testEmptyQuery(): Unit = {
    try {
      domaina.listItems
      fail
    } catch {
      case e => assertEquals("Client error : Invalid domain name domaina", e.getMessage)
    }
  }
}
