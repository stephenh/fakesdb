package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.xml.NodeSeq
import fakesdb._

class PutAttributes(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val itemName = params.getOrElse("ItemName", error("No item name"))
    val item = domain.getOrCreateItem(itemName)
    discoverAttributes(params).foreach((t: (String, String, Boolean)) => item.put(t._1, t._2, t._3))
    <PutAttributesResponse xmlns="http://sdb.amazonaws.com/doc/2007-11-07/">
      {responseMetaData}
    </PutAttributesResponse>
  }

  private def discoverAttributes(params: Params): List[(String, String, Boolean)] = {
    val attrs = new ListBuffer[(String, String, Boolean)]()
    var i = 0
    var stop = false
    while (!stop) {
      val attrName = params.get("Attribute."+i+".Name")
      val attrValue = params.get("Attribute."+i+".Value")
      val attrReplace = params.get("Attribute."+i+".Replace")
      if (attrName.isEmpty || attrValue.isEmpty) {
        if (i > 1) stop = true
      } else {
        attrs += ((attrName.get, attrValue.get, attrReplace.getOrElse("false").toBoolean))
      }
      i += 1
    }
    attrs.toList
  }

}
