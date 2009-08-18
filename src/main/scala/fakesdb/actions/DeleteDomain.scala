package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class DeleteDomain(data: Data) extends Action(data) {
  
  def handle(params: Params): NodeSeq = {
    data.deleteDomain(parseDomain(params))
    <DeleteDomainResponse xmlns="http://sdb.amazonaws.com/doc/2007-11-07/">
      {responseMetaData}
    </DeleteDomainResponse>
  }

}
