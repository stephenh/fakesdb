package fakesdb

import org.junit._
import org.junit.Assert._
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpledb.AmazonSimpleDBClient
import com.amazonaws.services.simpledb.model._
import scala.collection.JavaConversions._

object AbstractFakeSdbTest {
  val jetty = Jetty(8080)
  jetty.server.start()
}

abstract class AbstractFakeSdbTest {

  // start jetty
  AbstractFakeSdbTest.jetty
  val sdb = new AmazonSimpleDBClient(new BasicAWSCredentials("ignored", "ignored"))
  sdb.setEndpoint("http://127.0.0.1:8080")

  val domaina = "domaina"

  @Before
  def setUp(): Unit = {
    flush()
  }

  def flush(): Unit = {
    createDomain("_flush")
  }

  type KV = Tuple2[String, String]

  def add(domain: String, itemName: String, attrs: KV*): Unit = {
    _add(domain, itemName, attrs, None)
  }

  def add(domain: String, itemName: String, cond: UpdateCondition, attrs: KV*): Unit = {
    _add(domain, itemName, attrs, Some(cond))
  }

  def doesNotExist(attrName: String) = new UpdateCondition(attrName, null, false)

  def hasValue(attrName: String, attrValue: String) = new UpdateCondition(attrName, attrValue, true)

  def assertFails(code: String, message: String, block: => Unit) {
    try {
      block
      fail("Should have failed with " + message)
    } catch {
      case e: AmazonServiceException => {
        assertEquals(code, e.getErrorCode)
        assertEquals(message, e.getMessage)
      }
      case e: Exception => {
        assertEquals(message, e.getMessage)
      }
    }
  }

  def assertItems(domain: String, item: String, expectedItems: String*) {
    val result = sdb.getAttributes(new GetAttributesRequest(domain, item))
    val actualItems = result.getAttributes.map { a => a.getName + " = " + a.getValue }
    assertEquals(expectedItems.mkString("\n"), actualItems.mkString("\n"))
  }

  private def _add(domain: String, itemName: String, attrs: Seq[KV], cond: Option[UpdateCondition]): Unit = {
    val req = new PutAttributesRequest()
      .withDomainName(domain)
      .withItemName(itemName)
    for (a <- attrs) {
      val replace = asScalaBuffer(req.getAttributes).exists { _.getName == a._1 }
      req.withAttributes(new ReplaceableAttribute(a._1, a._2, replace))
    }
    cond match {
      case Some(c) => req.withExpected(c)
      case None =>
    }
    sdb.putAttributes(req)
  }

  protected def select(query: String): SelectResult = {
    sdb.select(new SelectRequest(query, true))
  }

  protected def select(query: String, nextToken: String): SelectResult = {
    sdb.select(new SelectRequest(query, true).withNextToken(nextToken))
  }

  protected def createDomain(name: String) {
    sdb.createDomain(new CreateDomainRequest(name))
  }

}
