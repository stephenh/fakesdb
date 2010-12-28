package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.xml.NodeSeq
import fakesdb._

class PutAttributes(data: Data) extends Action(data) with ConditionalChecking {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val itemName = params.getOrElse("ItemName", error("No item name"))
    val item = domain.getOrCreateItem(itemName)

    checkConditionals(item, params)

    discoverAttributes(params).foreach(a => {
      if (a._1 == "") {
        error("Empty attribute name")
      } else if (item.getAttributes.size < 256) {
        item.put(a._1, a._2, a._3)
      } else {
        error("Too many attributes")
      }
    })

    <PutAttributesResponse xmlns={namespace}>
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
        if (attrs find (a => a._1 == attrName.get && a._2 == attrValue.get) isEmpty) {
          attrs += ((attrName.get, attrValue.get, attrReplace.getOrElse("false").toBoolean))
        }
      }
      i += 1
    }
    attrs.toList
  }

}
