package fakesdb

import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._

case class SelectEval(output: OutputEval, from: String, where: Option[WhereEval])  {
  def select(data: Data): List[(String, List[(String,String)])] = {
    val domain = data.getDomain(from).getOrElse(error("Invalid from "+from))
    val items = where match {
      case Some(w) => w.filter(domain, domain.getItems.toList)
      case None => domain.getItems.toList
    }
    output.what(domain, items)
  }
}

sealed abstract class OutputEval {
  type OutputList = List[(String, List[(String, String)])]
  def what(domain: Domain, items: List[Item]): OutputList

  def flatAttrs(attrs: Iterator[Attribute]): List[(String, String)] = {
    attrs.flatMap((a: Attribute) => a.getValues.map((v: String) => (a.name, v))).toList
  }
}
case class CompoundOutput(attrNames: List[String]) extends OutputEval {
  def what(domain: Domain, items: List[Item]): OutputList = {
    items.map((item: Item) => {
      var i = (item.name, flatAttrs(item.getAttributes.filter((a: Attribute) => attrNames.contains(a.name))))
      if (attrNames.contains("itemName()")) {
        i = (i._1, ("itemName()", item.name) :: i._2)
      }
      i
    }).filter(_._2.size > 0)
  }
}
class AllOutput extends OutputEval {
  def what(domain: Domain, items: List[Item]): OutputList = {
    items.map((item: Item) => {
      (item.name, flatAttrs(item.getAttributes))
    }).filter(_._2.size > 0)
  }
}
class CountOutput extends OutputEval {
  def what(domain: Domain, items: List[Item]): OutputList = {
    List((domain.name, List(("Count", items.size.toString))))
  }
}

abstract class WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item]
}
case class SimpleWhereEval(name: String, op: String, value: String) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    op match {
      case "=" => items.filter((i: Item) => i.getAttribute(name) match {
        case Some(a) => a.getValues.exists((v: String) => v == value)
        case None => false
      }).toList
      case _ => items
    }
  }
}
case class CompoundWhereEval(sp: WhereEval, op: String, rest: WhereEval) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    op match {
      case "and" => sp.filter(domain, items).toList intersect rest.filter(domain, items).toList
      case "or" => sp.filter(domain, items).toList union rest.filter(domain, items).toList
      case _ => error("Invalid operator "+op)
    }
  }
}

// Use our own lexer because the "()" in "itemName()"
class SelectLexical extends StdLexical {
  override def token: Parser[Token] = (
   accept("itemName()".toList) ^^ { x => Identifier("itemName()") }
   | accept("count(*)".toList) ^^ { x => Keyword("count(*)") }
   | super.token )
}
object SelectParser extends StandardTokenParsers {
  override val lexical = new SelectLexical
  lexical.delimiters ++= List("*", ",", "=")
  lexical.reserved ++= List("select", "from", "where", "and", "or")

  def expr = (
    "select" ~ outputList ~ "from" ~ ident ~ "where" ~ where ^^ { case s ~ ol ~ fs ~ fi ~ ws ~ wc => SelectEval(ol, fi, Some(wc)) }
    | "select" ~ outputList ~ "from" ~ ident ^^ { case s ~ ol ~ fs ~ fi => SelectEval(ol, fi, None) }
  )

  def where: Parser[WhereEval] = (
    simplePredicate ~ simpleOp ~ where ^^ { case sp ~ op ~ rp => CompoundWhereEval(sp, op, rp) }
    | simplePredicate
  )

  def simpleOp = "and" | "or"

  def simplePredicate: Parser[WhereEval] = ident ~ op ~ stringLit ^^ { case i ~ o ~ v => SimpleWhereEval(i, o, v) }

  def op: Parser[String] = "="

  def outputList: Parser[OutputEval] = (
    "*" ^^ { case s => new AllOutput }
    | "count(*)" ^^ { _ => new CountOutput }
    | repsep(ident, ",") ^^ { attrNames: List[String] => CompoundOutput(attrNames) }
  )

  def makeSelectEval(input: String): SelectEval = {
    val tokens = new lexical.Scanner(input)
    phrase(expr)(tokens) match {
      case Success(selectEval, _) => selectEval
      case Failure(msg, _) => error(msg)
      case Error(msg, _) => error(msg)
    }
  }
}
