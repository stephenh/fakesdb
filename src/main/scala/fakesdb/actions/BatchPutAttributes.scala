package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedHashMap
import scala.xml.NodeSeq
import fakesdb._

class BatchPutAttributes(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    discoverAttributes(params).update(domain)
    <BatchPutAttributesResponse xmlns={namespace}>
      {responseMetaData}
    </BatchPutAttributesResponse>
  }

  private def discoverAttributes(params: Params): ItemUpdates = {
    val updates = new ItemUpdates
    var i = 0
    var stop = false
    while (!stop) {
      val itemName = params.get("Item."+i+".ItemName")
      if (itemName.isEmpty) {
        if (i > 1) stop = true
      } else {
        var j = 0
        var stop2 = false
        while (!stop2) {
          val attrName = params.get("Item."+i+".Attribute."+j+".Name")
          val attrValue = params.get("Item."+i+".Attribute."+j+".Value")
          val attrReplace = params.get("Item."+i+".Attribute."+j+".Replace")
          if (attrName.isEmpty || attrValue.isEmpty) {
            if (j > 1) stop2 = true
          } else {
            val replace = attrReplace.getOrElse("false").toBoolean
            updates.add(itemName.get, attrName.get, attrValue.get, replace)
          }
          j += 1
        }
      }
      i += 1
    }
    updates
  }

}
