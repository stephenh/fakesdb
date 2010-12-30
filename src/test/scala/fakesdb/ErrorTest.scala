package fakesdb

import org.junit.Assert._
import com.xerox.amazonws.sdb.ItemAttribute
import com.xerox.amazonws.sdb.SimpleDB

class ErrorTest extends AbstractFakeSdbTest {
  def testEmptyQuery(): Unit = {
    try {
      domaina.selectItems("SELECT * FROM domaina", null, true)
      fail
    } catch {
      case e => assertEquals("Client error : Invalid domain name domaina", e.getMessage)
    }
  }
}
