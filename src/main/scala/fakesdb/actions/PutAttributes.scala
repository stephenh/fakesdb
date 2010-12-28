package fakesdb.actions

import scala.collection.mutable.ListBuffer
import scala.xml.NodeSeq
import fakesdb._

class PutAttributes(data: Data) extends Action(data) {

  private val expectedNamePattern = """Expected\.(\d+)\.Name""".r

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
  
  private def checkConditionals(item: Item, params: Params) {
    for (condition <- discoverConditional(params)) {
      condition match {
        case (name, None) => for (f <- item.getAttributes.find(_.name == name)) throw new ConditionalCheckFailedException(condition)
        case (name, Some(value)) => item.getAttributes find (_.name == name) match {
          case None => throw new AttributeDoesNotExistException(name)
          case Some(attr) => if (attr.getValues.toList != List(value)) throw new ConditionalCheckFailedException(condition, attr.getValues.toList)
        }
      }
    }    
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

  private def discoverConditional(params: Params): Option[Tuple2[String, Option[String]]] = {
    val keys = params.keys find (k => k.startsWith("Expected") && k.endsWith("Name"))
    if (keys.isEmpty) {
      return None
    }
    if (keys.size > 1) {
      error("Only one condition may be specified")
    }
    val name = params.get(keys.head).get
    keys.head match {
      case expectedNamePattern(digit) => {
        for (v <- params.get("Expected.%s.Exists".format(digit))) {
          if (v == "false") {
            return Some((name, None))
          }
        }
        for (v <- params.get("Expected.%s.Value".format(digit))) {
          return Some((name, Some(v)))
        }
      }
    }
    None
  }

  class ConditionalCheckFailedException(message: String) extends SDBException("ConditionalCheckFailed", message) {
    def this(condition: Tuple2[String, Option[String]]) = {
      this("Attribute (%s) value exists".format(condition._1))
    }

    def this(condition: Tuple2[String, Option[String]], actual: List[String]) = {
      this("Attribute (%s) value is (%s) but was expected (%s)".format(condition._1, actual, condition._2.get))
    }
  }

  class AttributeDoesNotExistException(name: String)
    extends SDBException("AttributeDoesNotExist", "Attribute (%s) does not exist".format(name))
}
