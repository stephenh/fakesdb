package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class CreateDomain(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domainName = params.getOrElse("DomainName", error("No domain name"))
    if (domainName == "_flush") {
      data.flush() // The special one
    } else {
      data.getOrCreateDomain(domainName)
    }
    <CreateDomainResponse xmlns="http://sdb.amazonaws.com/doc/2007-11-07/">
      {responseMetaData}
    </CreateDomainResponse>
  }

}
