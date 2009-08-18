package fakesdb

import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._

sealed abstract class QueryEval {
  def eval(item: Item): Boolean
  def sort(items: List[Item]): List[Item] = items
  def evalAndSort(domain: Domain): List[Item] = {
    sort(domain.getItems.filter(eval(_)).toList)
  }
}

case class EvalSimplePredicate(name: String, op: String, value: String) extends QueryEval {
  def eval(item: Item): Boolean = {
    val func = getFunc(op)
    item.getAttribute(name) match {
      case Some(a) => a.getValues.find(func).isDefined
      case None => false
    }
  }
  def getFunc(op: String): Function1[String, Boolean] = op match {
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

case class EvalCompoundPredicate(left: QueryEval, op: String, right: QueryEval) extends QueryEval {
  def eval(item: Item): Boolean = {
    op match {
      case "and" => left.eval(item) && right.eval(item)
      case "or" => left.eval(item) || right.eval(item)
      case "intersection" => left.eval(item) && right.eval(item)
      case "union" => left.eval(item) || right.eval(item)
      case _ => error("Invalid operator "+op)
    }
  }
}

case class EvalSetNegate(other: QueryEval) extends QueryEval {
  def eval(item: Item) = !other.eval(item)
}

case class EvalSort(other: QueryEval, name: String, way: String) extends QueryEval {
  def eval(item: Item) = other.eval(item)
  override def sort(items: List[Item]) = {
    items.sort((a, b) => {
      val av = a.getAttribute(name) match { case Some(a) => a.getValues.next ; case None => "" }
      val bv = b.getAttribute(name) match { case Some(a) => a.getValues.next ; case None => "" }
      way match {
        case "asc" => av < bv
        case "desc" => av > bv
        case _ => av < bv
      }
    })
  }
}

// Use our own lexer because the "-" in "starts-with" was causing "starts-with"
// to be prematurely recognized as the identifier "start" instead of a delimiter
class OurLexical extends StdLexical {
  override def token: Parser[Token] = (
   accept("starts-with".toList) ^^ { x => Keyword("starts-with") }
   | accept("does-not-start-with".toList) ^^ { x => Keyword("does-not-start-with") }
   | super.token )
}

object QueryParser extends StandardTokenParsers {
  override val lexical = new OurLexical()
  lexical.delimiters ++= List("[", "]", "=", "!=", "<", ">", ">=", "<=")
  lexical.reserved ++= List("and", "or", "not", "union", "intersection", "sort", "asc", "desc")

  def expr: Parser[QueryEval] = (
    setPredicate ~ "sort" ~ stringLit ~ ("asc" | "desc") ^^ { case sp ~ s ~ key ~ way => EvalSort(sp, key, way) }
    | setPredicate ~ "sort" ~ stringLit ^^ { case sp ~ s ~ key => EvalSort(sp, key, null) }
    | setPredicate
  )
  def setPredicate: Parser[QueryEval] = "not" ~ setPredicate ^^ { case n ~ sp => EvalSetNegate(sp) } |
    bracketPredicate ~ setOperator ~ setPredicate ^^ { case l ~ o ~ r => EvalCompoundPredicate(l, o, r) } |
    bracketPredicate ^^ { case sp => sp }
  def setOperator: Parser[String] = "union" | "intersection"
  def bracketPredicate: Parser[QueryEval] = "[" ~ compoundPredicate ~ "]" ^^ { case l ~ cp ~ r => cp }

  def compoundPredicate: Parser[QueryEval] =
    simplePredicate ~ compoundOperator ~ compoundPredicate  ^^ { case l ~ o ~ r => EvalCompoundPredicate(l, o, r) } |
    simplePredicate ^^ { case s => s }
  def compoundOperator: Parser[String] = "and" | "or"

  def simplePredicate: Parser[QueryEval] = name ~ simpleOperator ~ value ^^ { case n ~ o ~ v => EvalSimplePredicate(n, o, v) }
  def simpleOperator: Parser[String] =  "=" | "!=" | "<" | ">" | "<=" | ">=" | "starts-with" | "does-not-start-with"
  def name: Parser[String] = stringLit
  def value: Parser[String] = stringLit

  def makeQueryEval(input: String): QueryEval = {
    val tokens = new lexical.Scanner(input)
    phrase(expr)(tokens) match {
      case Success(queryEval, _) => queryEval
      case Failure(msg, _) => error(msg)
      case Error(msg, _) => error(msg)
    }
  }
}
