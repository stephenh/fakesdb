package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

abstract class Action(data: Data) {

  def handle(params: Params): NodeSeq

  protected def responseMetaData() = {
    <ResponseMetadata><RequestId>{requestId}</RequestId><BoxUsage>0</BoxUsage></ResponseMetadata>
  }

  protected def parseDomain(params: Params): Domain = {
    val domainName = params.getOrElse("DomainName", sys.error("No domain name"))
    return data.getDomain(domainName).getOrElse(sys.error("Invalid domain name "+domainName))
  }

  val namespace = "http://sdb.amazonaws.com/doc/2009-04-15/"

  val requestId = Action.requestCounter.incrementAndGet()

}

object Action {
  private val requestCounter = new java.util.concurrent.atomic.AtomicInteger()
}
