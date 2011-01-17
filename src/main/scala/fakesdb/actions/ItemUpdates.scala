package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedHashMap
import fakesdb._

/** itemName -> [attrName -> AttributeUpdate] */
class ItemUpdates extends LinkedHashMap[String, LinkedHashMap[String, AttributeUpdate]] {
  def add(itemName: String, attrName: String, attrValue: String, replace: Boolean): Unit = {
    val attrs = getOrElseUpdate(itemName, new LinkedHashMap[String, AttributeUpdate])
    val attr = attrs.getOrElseUpdate(attrName, new AttributeUpdate(replace))
    attr.values += attrValue
  }

  def update(domain: Domain): Unit = {
    foreach { case (itemName, attrs) => {
      attrs.foreach { case (attrName, attrUpdate) => {
        domain.getOrCreateItem(itemName).put(attrName, attrUpdate.values, attrUpdate.replace)
      }}
    }}
  }

}

class AttributeUpdate(val replace: Boolean) {
  val values = new ListBuffer[String]()
}
