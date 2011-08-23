package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class Select(data: Data) extends Action(data) {

  override def handle(params: Params): NodeSeq = {
    val nextToken = params.get("NextToken") map { _.toInt }
    val itemsData = params.get("SelectExpression") match {
      case Some(s) => val se = SelectParser.makeSelectEval(s) ; se.select(data, nextToken)
      case None => sys.error("No select expression")
    }
    val items = itemsData._1
    val itemsLength = itemsData._2
    val newNextToken = if (itemsData._3) List(itemsLength) else List()
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
        {for (token <- newNextToken) yield
          <NextToken>{token}</NextToken>
        }
      </SelectResult>
      {responseMetaData}
    </SelectResponse>
  }

}
