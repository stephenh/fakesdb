package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

abstract class Action(data: Data) {

  def handle(params: Params): NodeSeq

  protected def responseMetaData() = {
    <ResponseMetadata><RequestId>0</RequestId><BoxUsage>0</BoxUsage></ResponseMetadata>
  }

  protected def parseDomain(params: Params): Domain = {
    val domainName = params.getOrElse("DomainName", error("No domain name"))
    return data.getDomain(domainName).getOrElse(error("Invalid domain name "+domainName))
  }

  protected def namespace = "http://sdb.amazonaws.com/doc/2009-04-15/"

}
