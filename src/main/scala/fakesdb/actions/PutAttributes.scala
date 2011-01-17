package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedHashMap
import scala.xml.NodeSeq
import fakesdb._

class PutAttributes(data: Data) extends Action(data) with ConditionalChecking {

  def handle(params: Params): NodeSeq = {
    val domain = parseDomain(params)
    val itemName = params.getOrElse("ItemName", throw new MissingItemNameException)
    val item = domain.getOrCreateItem(itemName)

    checkConditionals(item, params)

    discoverAttributes(params).foreach { case (name, attr) => {
      if (name == "") {
        throw new EmptyAttributeNameException
      } else if (item.getAttributes.size + attr.values.size < 256) {
        item.put(name, attr.values, attr.replace)
      } else {
        throw new NumberItemAttributesExceededException
      }
    }}

    <PutAttributesResponse xmlns={namespace}>
      {responseMetaData}
    </PutAttributesResponse>
  }

  private def discoverAttributes(params: Params): LinkedHashMap[String, AttributeUpdate] = {
    val attrs = new LinkedHashMap[String, AttributeUpdate]
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
        val attr = attrs.getOrElseUpdate(attrName.get, new AttributeUpdate(replace))
        attr.values += attrValue.get
      }
      i += 1
    }
    attrs
  }
}

class AttributeUpdate(val replace: Boolean) {
  val values = new ListBuffer[String]()
}

class NumberItemAttributesExceededException extends SDBException(409, "NumberItemAttributesExceeded", "Too many attributes in this item")

class EmptyAttributeNameException
  extends SDBException(400, "InvalidParameterValue", "Value () for parameter Name is invalid. The empty string is an illegal attribute name")

class MissingItemNameException
  extends SDBException(400, "MissingParameter", "The request must contain the parameter ItemName.")
