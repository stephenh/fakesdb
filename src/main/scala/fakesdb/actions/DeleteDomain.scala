package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class DeleteDomain(data: Data) extends Action(data) {
  
  def handle(params: Params): NodeSeq = {
    data.deleteDomain(parseDomain(params))
    <DeleteDomainResponse xmlns={namespace}>
      {responseMetaData}
    </DeleteDomainResponse>
  }

}
