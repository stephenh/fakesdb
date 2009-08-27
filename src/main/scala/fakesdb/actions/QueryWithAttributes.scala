package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class QueryWithAttributes(data: Data) extends GetAttributes(data) {

  override def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val items = params.get("QueryExpression") match {
      case Some(q) if q.size > 0 => QueryParser.makeQueryEval(q).eval(domain.getItems.toList)
      case _ => domain.getItems.toList
    }
    val requested = discoverAttributes(params)
    <QueryWithAttributesResponse xmlns="http://sdb.amazonaws.com/doc/2007-11-07/">
      <QueryWithAttributesResult>
        {for (item <- items if filter(item, requested).hasNext) yield
          <Item>
            <Name>{item.name}</Name>
            {for (nv <- filter(item, requested)) yield
              <Attribute><Name>{nv._1.name}</Name><Value>{nv._2}</Value></Attribute>
            }
          </Item>
        }
      </QueryWithAttributesResult>
      {responseMetaData}
    </QueryWithAttributesResponse>
  }

}

