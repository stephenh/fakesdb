package fakesdb.actions

import fakesdb._

trait ConditionalChecking {

  private val expectedNamePattern = """Expected\.(\d+)\.Name""".r
  private val expectedNamePattern2 = """Expected\.Name""".r

  def checkConditionals(item: Item, params: Params) {
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

  private def discoverConditional(params: Params): Option[Tuple2[String, Option[String]]] = {
    val keys = params.keys find (k => k.startsWith("Expected") && k.endsWith("Name"))
    if (keys.isEmpty) {
      return None
    }
    if (keys.size > 1) {
      sys.error("Only one condition may be specified")
    }
    val name = params.get(keys.head).get
    keys.head match {
      case expectedNamePattern2() => {
        // the aws jdk sends "Expected.Name" with no digit
        for (v <- params.get("Expected.Exists")) {
          if (v == "false") {
            return Some((name, None))
          }
        }
        for (v <- params.get("Expected.Value")) {
          return Some((name, Some(v)))
        }
      }
      case expectedNamePattern(digit) => {
        // typica sent "Expected.x.Name" with a digit
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
}

class ConditionalCheckFailedException(message: String) extends SDBException(409, "ConditionalCheckFailed", message) {
  def this(condition: Tuple2[String, Option[String]]) = {
    this("Attribute (%s) value exists".format(condition._1))
  }

  def this(condition: Tuple2[String, Option[String]], actual: List[String]) = {
    this("Attribute (%s) value is (%s) but was expected (%s)".format(condition._1, actual, condition._2.get))
  }
}

class AttributeDoesNotExistException(name: String)
  extends SDBException(404, "AttributeDoesNotExist", "Attribute (%s) does not exist".format(name))
