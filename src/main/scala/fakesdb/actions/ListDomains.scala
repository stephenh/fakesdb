package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class ListDomains(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    <ListDomainsResponse xmlns="http://sdb.amazonaws.com/doc/2007-11-07/">
      <ListDomainsResult>
      {for (domain <- data.getDomains) yield
        <DomainName>{domain.name}</DomainName>
      }
      </ListDomainsResult>
      {responseMetaData}
    </ListDomainsResponse>
  }

}
