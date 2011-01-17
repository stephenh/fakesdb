package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class PutAttributes(data: Data) extends Action(data) with ConditionalChecking {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val itemName = params.getOrElse("ItemName", throw new MissingItemNameException)
    val item = domain.getOrCreateItem(itemName)

    checkConditionals(item, params)

    discoverAttributes(itemName, params).update(domain)

    <PutAttributesResponse xmlns={namespace}>
      {responseMetaData}
    </PutAttributesResponse>
  }

  private def discoverAttributes(itemName: String, params: Params): ItemUpdates = {
    val updates = new ItemUpdates
    var i = 0
    var stop = false
    while (!stop) {
      val attrName = params.get("Attribute."+i+".Name")
      val attrValue = params.get("Attribute."+i+".Value")
      val attrReplace = params.get("Attribute."+i+".Replace")
      if (attrName.isEmpty || attrValue.isEmpty) {
        if (i > 1) stop = true
      } else {
        val replace = attrReplace.getOrElse("false").toBoolean
        updates.add(itemName, attrName.get, attrValue.get, replace)
      }
      i += 1
    }
    updates
  }
}

class NumberItemAttributesExceededException extends SDBException(409, "NumberItemAttributesExceeded", "Too many attributes in this item")

class EmptyAttributeNameException
  extends SDBException(400, "InvalidParameterValue", "Value () for parameter Name is invalid. The empty string is an illegal attribute name")

class MissingItemNameException
  extends SDBException(400, "MissingParameter", "The request must contain the parameter ItemName.")
