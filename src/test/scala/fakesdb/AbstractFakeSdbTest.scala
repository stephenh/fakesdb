package fakesdb

import org.junit._
import org.junit.Assert._
import com.xerox.amazonws.sdb.Condition
import com.xerox.amazonws.sdb.ItemAttribute
import com.xerox.amazonws.sdb.SDBException
import com.xerox.amazonws.sdb.SimpleDB
import com.xerox.amazonws.sdb.{Domain => SDomain}
import scala.collection.JavaConversions._
import scala.collection.mutable.HashSet

object AbstractFakeSdbTest {
  val jetty = Jetty(8080)
  jetty.server.start()
}

abstract class AbstractFakeSdbTest {

  // start jetty
  AbstractFakeSdbTest.jetty
  // typica does not respect ports 9999
  // val sdb = new SimpleDB(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY"), false)
  val sdb = new SimpleDB("ignored", "ignored", false, "127.0.0.1")
  val domaina = sdb.getDomain("domaina")

  @Before
  def setUp(): Unit = {
    flush()
  }

  def flush(): Unit = {
    sdb.createDomain("_flush")
  }

  type KV = Tuple2[String, String]

  def add(domain: SDomain, itemName: String, attrs: KV*): Unit = {
    _add(domain, itemName, attrs, None)
  }

  def add(domain: SDomain, itemName: String, cond: Condition, attrs: KV*): Unit = {
    _add(domain, itemName, attrs, Some(cond))
  }

  def doesNotExist(attrName: String) = new Condition(attrName, false)

  def hasValue(attrName: String, attrValue: String) = new Condition(attrName, attrValue)

  def assertFails(code: String, message: String, block: => Unit) {
    try {
      block
      fail("Should have failed with " + message)
    } catch {
      case e: SDBException => {
        val error = e.getErrors.get(0)
        assertEquals(code, error.getCode)
        assertEquals(message, error.getMessage)
      }
      case e: Exception => {
        assertEquals(message, e.getMessage)
      }
    }
  }

  private def _add(domain: SDomain, itemName: String, attrs: Seq[KV], cond: Option[Condition]): Unit = {
    val item = domain getItem itemName
    val seen = new HashSet[String]()
    val list = attrs.map { attr =>
      val replace = seen.add(attr._1)
      new ItemAttribute(attr._1, attr._2, replace)
    }
    cond match {
      case Some(c) => item.putAttributes(list, List(c))
      case None => item.putAttributes(list)
    }
  }

}
