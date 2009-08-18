package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class Query(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val items = params.get("QueryExpression") match {
      case Some(q) if q.size > 0 => val qe = QueryParser.makeQueryEval(q) ; qe.sort(domain.getItems.filter(qe.eval(_)).toList).elements
      case _ => domain.getItems
    }
    <QueryResponse xmlns="http://sdb.amazonaws.com/doc/2007-11-07/">
      <QueryResult>
        {for (item <- items) yield
        <ItemName>{item.name}</ItemName>
        }
      </QueryResult>
      {responseMetaData}
    </QueryResponse>
  }

}
