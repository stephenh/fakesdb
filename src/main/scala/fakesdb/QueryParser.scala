package fakesdb

import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._

sealed abstract class QueryEval {
  def eval(items: List[Item]): List[Item]
  def getQueriedAttributes: List[String]
}

case class EvalCompoundPredicate(op: String)(left: QueryEval, right: QueryEval) extends QueryEval {
  def eval(items: List[Item]) = op match {
    case "union" => left.eval(items) union right.eval(items)
    case "intersection" => left.eval(items) intersect right.eval(items)
    case _ => error("Invalid operator")
  }
  def getQueriedAttributes = left.getQueriedAttributes ++ right.getQueriedAttributes
}

case class EvalSort(filter: QueryEval, sort: Sort) extends QueryEval {
  def eval(items: List[Item]) = {
      if (!filter.getQueriedAttributes.contains(sort.name)) {
        error("Invalid sort attribute "+sort.name)
      }
    filter.eval(items).sortWith((a, b) => {
      val av = a.getAttribute(sort.name) match { case Some(a) => a.getValues.next ; case None => "" }
      val bv = b.getAttribute(sort.name) match { case Some(a) => a.getValues.next ; case None => "" }
      sort.way match {
        case "asc" => av < bv
        case "desc" => av > bv
        case _ => error("Invalid sort")
      }
    })
  }
  def getQueriedAttributes = filter.getQueriedAttributes
}

case class Sort(name: String, way: String)

case class AttributeQueryEval(attributeEval: AttributeEval, negate: Boolean) extends QueryEval {
  def eval(items: List[Item]): List[Item] = {
    // get the name to make sure its the same in all of the attribute comparisons
    attributeEval.name
    items.filter((i) => {
      val hasOne = i.getAttributes.find((a) => {
        a.getValues.find(attributeEval.eval(_)).isDefined
      }).isDefined
      if (negate) !hasOne else hasOne
    })
  }
  def getQueriedAttributes = if (negate) List() else List(attributeEval.name)
}

abstract class AttributeEval {
  def eval(value: String): Boolean
  def name: String
}

case class SimpleAttributeEval(name: String, op: String, value: String) extends AttributeEval {
  def eval(value: String) = {
    getFunc()(value)
  }
  private def getFunc(): Function1[String, Boolean] = op match {
    case "=" => _ == value
    case "!=" => _ != value
    case "<" => _ < value
    case ">" => _ > value
    case "<=" => _ <= value
    case ">=" => _ >= value
    case "starts-with" => _.startsWith(value)
    case "does-not-start-with" => !_.startsWith(value)
    case _ => error("Invalid operator "+op)
  }
}

case class CompoundAttributeEval(op: String)(left: AttributeEval, right: AttributeEval) extends AttributeEval {
  def eval(value: String): Boolean = op match {
    case "and" => left.eval(value) && right.eval(value)
    case "or" => left.eval(value) || right.eval(value)
    case _ => error("Invalid operator "+op)
  }
  def name = {
    if (left.name != right.name) {
      error("Attribute comparison names do not match")
    }
    left.name
  }
}

// Use our own lexer because the "-" in "starts-with" was causing "starts-with"
// to be prematurely recognized as the identifier "start" instead of a delimiter
class OurLexical extends StdLexical {
  override def token: Parser[Token] =
   ( accept("starts-with".toList) ^^ { x => Keyword("starts-with") }
   | accept("does-not-start-with".toList) ^^ { x => Keyword("does-not-start-with") }
   | super.token
  )
}

object QueryParser extends StandardTokenParsers {
  override val lexical = new OurLexical()
  lexical.delimiters ++= List("[", "]", "=", "!=", "<", ">", ">=", "<=")
  lexical.reserved ++= List("and", "or", "not", "union", "intersection", "sort", "asc", "desc")

  def eval: Parser[QueryEval] =
    ( predicates ~ sort ^^ { case p ~ s => EvalSort(p, s) }
    | predicates
  )

  def sort =
    ( "sort" ~ stringLit ~ ("asc" | "desc")  ^^ { case s ~ key ~ way => Sort(key, way) }
    | "sort" ~ stringLit ^^ { case s ~ key => Sort(key, "asc") }
  )

  def predicates = predicate * (("union" | "intersection") ^^ { case o => EvalCompoundPredicate(o) _ })

  def predicate =
    ( "not" ~ "[" ~ attributePredicate ~ "]" ^^ { case n ~ l ~ ap ~ r => AttributeQueryEval(ap, true) }
    | "[" ~ attributePredicate ~ "]" ^^ { case l ~ ap ~ r => AttributeQueryEval(ap, false) }
  )

  def attributePredicate = attributeComparison * (("and" | "or") ^^ { case o => CompoundAttributeEval(o) _ } )

  def attributeComparison = stringLit ~ attributeOperator ~ stringLit ^^ { case n ~ o ~ v => SimpleAttributeEval(n, o, v) }

  def attributeOperator = "=" | "!=" | "<" | ">" | "<=" | ">=" | "starts-with" | "does-not-start-with"

  def makeQueryEval(input: String): QueryEval = {
    val tokens = new lexical.Scanner(input)
    phrase(eval)(tokens) match {
      case Success(queryEval, _) => queryEval
      case Failure(msg, _) => error(msg)
      case Error(msg, _) => error(msg)
    }
  }
}
