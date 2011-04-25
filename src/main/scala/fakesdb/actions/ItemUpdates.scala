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

  def add(itemName: String, attrName: String): Unit = {
    val attrs = getOrElseUpdate(itemName, new LinkedHashMap[String, AttributeUpdate])
    val attr = attrs.getOrElseUpdate(attrName, new AttributeUpdate(false))
  }

  def add(itemName: String): Unit = {
    val attrs = getOrElseUpdate(itemName, new LinkedHashMap[String, AttributeUpdate])
  }

  def update(domain: Domain): Unit = {
    checkSize()
    foreach { case (itemName, attrs) => {
      attrs.foreach { case (attrName, attrUpdate) => {
        domain.getOrCreateItem(itemName).put(attrName, attrUpdate.values, attrUpdate.replace)
      }}
    }}
  }

  def delete(domain: Domain): Unit = {
    checkSize()
    foreach { case (itemName, attrs) => {
      val item = domain.getItem(itemName) match {
        case Some(item) => {
          if (attrs.isEmpty) {
            domain.deleteItem(item)
          } else {
            attrs.foreach { case (attrName, replace) => {
              item.delete(attrName)
            }}
            domain.deleteIfEmpty(item)
          }
        }
        case _ =>
      }
    }}
  }

  private def checkSize() = {
    if (size > 25) {
       throw new NumberSubmittedItemsExceeded
    }
  }
}

class AttributeUpdate(val replace: Boolean) {
  val values = new ListBuffer[String]()
}
