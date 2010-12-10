package fakesdb

import org.junit._
import com.xerox.amazonws.sdb.ItemAttribute
import com.xerox.amazonws.sdb.SimpleDB

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

  def add(domain: com.xerox.amazonws.sdb.Domain, itemName: String, attrs: Tuple2[String, String]*) = {
    val item = domain getItem itemName
    val list = new java.util.ArrayList[ItemAttribute]()
    val seen = new scala.collection.mutable.HashSet[String]()
    for (attr <- attrs) {
      val replace = !seen.contains(attr._1)
      list.add(new ItemAttribute(attr._1, attr._2, replace))
      seen += attr._1
    }
    item.putAttributes(list)
  }

}
