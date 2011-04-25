package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class Select(data: Data) extends Action(data) {

  override def handle(params: Params): NodeSeq = {
    val drop = params.get("NextToken") match {
      case Some(s) => data.getItemsCountForRequest(s) match {
        case Some(v) => v
        case None => 0
      }
      case None => 0
    }
    val itemsData = params.get("SelectExpression") match {
      case Some(s) => val se = SelectParser.makeSelectEval(s) ; se.select(data, drop)
      case None => error("No select expression")
    }
    data.putRequestItemsCount(requiestId.toString(), drop + itemsData._2)
    val items = itemsData._1
    <SelectResponse xmlns={namespace}>
      <SelectResult>
        {for (item <- items) yield
          <Item>
            <Name>{item._1}</Name>
            {for (nv <- item._2) yield
              <Attribute><Name>{nv._1}</Name><Value>{nv._2}</Value></Attribute>
            }
          </Item>
        }
      </SelectResult>
      {responseMetaData}
    </SelectResponse>
  }

}
