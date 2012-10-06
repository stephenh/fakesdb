package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedHashMap
import scala.xml.NodeSeq
import fakesdb._

class BatchDeleteAttributes(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    discoverAttributes(params).delete(domain)
    <BatchDeleteAttributesResponse xmlns={namespace}>
      {responseMetaData}
    </BatchDeleteAttributesResponse>
  }

  private def discoverAttributes(params: Params): ItemUpdates = {
    val updates = new ItemUpdates()
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
          //values currently not supported.
          //val attrValue = params.get("Item."+i+".Attribute."+j+".Value")
          if (attrName.isEmpty) {
            if (j > 1) stop2 = true
            updates.add(itemName.get)
          } else {
            updates.add(itemName.get, attrName.get)
          }
          j += 1
        }
      }
      i += 1
    }
    updates
  }

}
