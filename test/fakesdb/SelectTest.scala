package fakesdb

import junit.framework.Assert._
import com.xerox.amazonws.sdb._

class SelectTest extends AbstractSimpleSdbTest {

  override def setUp(): Unit = {
    sdb.createDomain("domaina")
  }

  def testCount(): Unit = {
    domaina.selectItems("select count(*) from domaina", null)
  }
}
