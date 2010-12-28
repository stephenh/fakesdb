package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class Select(data: Data) extends Action(data) {

  override def handle(params: Params): NodeSeq = {
    val items = params.get("SelectExpression") match {
      case Some(s) => val se = SelectParser.makeSelectEval(s) ; se.select(data)
      case None => error("No select expression")
    }
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
