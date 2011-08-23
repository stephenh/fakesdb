package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.xml.NodeSeq
import fakesdb._

class GetAttributes(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val itemName = params.getOrElse("ItemName", sys.error("No item name"))
    val items = domain.getItem(itemName) match {
      case Some(item) => List(item)
      case None => List()
    }
    val requested = discoverAttributes(params)
    <GetAttributesResponse xmlns={namespace}>
      <GetAttributesResult>
        {for (item <- items) yield
          {for (nv <- filter(item, requested)) yield
            <Attribute><Name>{nv._1.name}</Name><Value>{nv._2}</Value></Attribute>
          }
        }
      </GetAttributesResult>
      {responseMetaData}
    </GetAttributesResponse>
  }

  protected def filter(item: Item, requested: List[String]): Iterator[(Attribute, String)] = {
    val attrs = item.getAttributes.filter((a: Attribute) => requested.isEmpty || requested.contains(a.name))
    attrs.flatMap((a: Attribute) => a.getValues.map((v: String) => (a, v)))
  }

  protected def discoverAttributes(params: Params): List[String] = {
    val requested = new ListBuffer[String]()
    var i = 0
    var stop = false
    while (!stop) {
      val attrName = params.get("AttributeName."+i)
      if (attrName.isEmpty) {
        if (i > 1) stop = true
      } else {
        requested += attrName.get
      }
      i += 1
    }
    requested.toList
  }

}
