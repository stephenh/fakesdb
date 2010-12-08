package fakesdb

import scala.collection.mutable.ListBuffer
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._
import scala.util.parsing.input.CharArrayReader.EofCh

case class SelectEval(output: OutputEval, from: String, where: WhereEval, order: OrderEval, limit: LimitEval)  {
  def select(data: Data): List[(String, List[(String,String)])] = {
    val domain = data.getDomain(from).getOrElse(error("Invalid from "+from))
    val items = limit.limit(order.sort(where.filter(domain, domain.getItems.toList)))
    output.what(domain, items)
  }
}

abstract class OutputEval {
  type OutputList = List[(String, List[(String, String)])]
  def what(domain: Domain, items: List[Item]): OutputList

  protected def flatAttrs(attrs: Iterator[Attribute]): List[(String, String)] = {
    attrs.flatMap((a: Attribute) => a.getValues.map((v: String) => (a.name, v))).toList
  }
}
case class CompoundOutput(attrNames: List[String]) extends OutputEval {
  def what(domain: Domain, items: List[Item]): OutputList = {
    items.map((item: Item) => {
      var i = (item.name, flatAttrs(item.getAttributes.filter((a: Attribute) => attrNames.contains(a.name))))
      if (attrNames.contains("itemName()")) { // ugly
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
    List(("Domain", List(("Count", items.size.toString))))
  }
}

abstract class WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item]

  protected def getFunc(op: String): Function2[String, String, Boolean] = op match {
    case "=" => _ == _
    case "!=" => _ != _
    case ">" => _ > _
    case "<" => _ < _
    case ">=" => _ >= _
    case "<=" => _ <= _
    case "like" => (v1, v2) => v1.matches(v2.replaceAll("%", ".*"))
    case "not-like" => (v1, v2) => !v1.matches(v2.replaceAll("%", ".*"))
  }
}
case class NoopWhere() extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = items
}
case class SimpleWhereEval(name: String, op: String, value: String) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    val func = getFunc(op)
    items.filter((i: Item) => i.getAttribute(name) match {
      case Some(a) => a.getValues.find(func(_, value)).isDefined
      case None => false
    }).toList
  }
}

abstract class LimitEval {
  def limit(items: List[Item]): List[Item]
}
case class NoopLimit() extends LimitEval {
  def limit(items: List[Item]) = items
}
case class SomeLimit(limit: Int) extends LimitEval {
  def limit(items: List[Item]) = items take limit
}

case class EveryEval(name: String, op: String, value: String) extends WhereEval {
  override def filter(domain: Domain, items: List[Item]): List[Item] = {
    val func = getFunc(op)
    items.filter((i: Item) => i.getAttribute(name) match {
      case Some(a) => a.getValues.forall(func(_, value))
      case None => false
    }).toList
  }
}
case class CompoundWhereEval(sp: WhereEval, op: String, rest: WhereEval) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    op match {
      case "intersection" => sp.filter(domain, items).toList intersect rest.filter(domain, items).toList
      case "and" => sp.filter(domain, items).toList intersect rest.filter(domain, items).toList
      case "or" => sp.filter(domain, items).toList union rest.filter(domain, items).toList
      case _ => error("Invalid operator "+op)
    }
  }
}
case class IsNullEval(name: String, isNull: Boolean) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    items.filter((i: Item) => if (isNull) {
      i.getAttribute(name).isEmpty
    } else {
      i.getAttribute(name).isDefined
    }).toList
  }
}
case class IsBetweenEval(name: String, lower: String, upper: String) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    items.filter((i: Item) => i.getAttribute(name) match {
      case Some(a) => a.getValues.exists(_ >= lower) && a.getValues.exists(_ <= upper)
      case None => false
    }).toList
  }
}
case class InEval(name: String, values: List[String]) extends WhereEval {
  def filter(domain: Domain, items: List[Item]): List[Item] = {
    items.filter((i: Item) => i.getAttribute(name) match {
      case Some(a) => a.getValues.exists(values.contains(_))
      case None => false
    }).toList
  }
}

abstract class OrderEval {
  def sort(items: List[Item]): List[Item]
}
case class NoopOrder() extends OrderEval {
  def sort(items: List[Item]) = items
}
case class SimpleOrderEval(name: String, way: String) extends OrderEval {
  def sort(items: List[Item]): List[Item] = {
    val comp = (lv: String, rv: String) => way match {
      case "desc" => lv > rv
      case _ => lv < rv
    }
    items.sortWith((l, r) => {
      comp(resolveValue(l), resolveValue(r))
    })
  }
  def resolveValue(item: Item) = {
    if (name == "itemName()") {
      item.name
    } else {
      item.getAttribute(name) match {
        case Some(a) => a.getValues.next
        case None => "" // default value
      }
    }
  }
}

// Use our own lexer because the "()" in "itemName()"
class SelectLexical extends StdLexical {
  override def token: Parser[Token] =
   ( accept("itemName()".toList) ^^^ { Identifier("itemName()") }
   | acceptInsensitiveSeq("count(*)".toList) ^^^ { Keyword("count(*)") }
   | '\'' ~> rep(chrWithDoubleTicks) <~ '\'' ^^ { chars => StringLit(chars mkString "") }
   | '"' ~> rep(chrWithDoubleQuotes) <~ '"' ^^ { chars => StringLit(chars mkString "") }
   | letter ~ rep( letter | digit | '_' | '.' | '-' ) ^^ { case first ~ rest => processIdent(first :: rest mkString "") }
   | super.token
  )

  // Allow case insensitive keywords by lower casing everything
  override protected def processIdent(name: String) =
    if (reserved contains name.toLowerCase) Keyword(name.toLowerCase) else Identifier(name)

  // Wow this works--inline acceptSeq and acceptIf, but adds _.toLowerCase
  def acceptInsensitiveSeq[ES <% Iterable[Elem]](es: ES): Parser[List[Elem]] =
    es.foldRight[Parser[List[Elem]]](success(Nil)){(x, pxs) => acceptIf(_.toLower == x)("`"+x+"' expected but " + _ + " found") ~ pxs ^^ mkList}

  def chrWithDoubleTicks = ('\'' ~ '\'') ^^^ '\'' | chrExcept('\'', EofCh)

  def chrWithDoubleQuotes = ('"' ~ '"') ^^^ '"' | chrExcept('"', EofCh)

}

object SelectParser extends StandardTokenParsers {
  override val lexical = new SelectLexical
  lexical.delimiters ++= List("*", ",", "=", "!=", ">", "<", ">=", "<=", "(", ")", "`")
  lexical.reserved ++= List("select", "from", "where", "and", "or", "like", "not", "is", "null", "between", "every", "in", "order", "by", "asc", "desc", "intersection", "limit", "count(*)")

  def expr = ("select" ~> outputList) ~ (("from" ~> ident) | ("from" ~> "`" ~> ident <~ "`")) ~ whereClause ~ order ~ limit ^^ { case ol ~ i ~ w ~ o ~ l => SelectEval(ol, i, w, o, l) }

  def order: Parser[OrderEval] =
    ( "order" ~> "by" ~> ident ~ ("asc" | "desc") ^^ { case i ~ way => SimpleOrderEval(i, way) }
    | "order" ~> "by" ~> ident ^^ { case i => SimpleOrderEval(i, "asc") }
    | success(NoopOrder())
  )

  def limit: Parser[LimitEval] =
    ( "limit" ~ numericLit ^^ { case l ~ num => SomeLimit(num.toInt) }
    | success(NoopLimit())
  )

  def whereClause: Parser[WhereEval] =
    ( "where" ~> where
    | success(new NoopWhere)
  )
  def where: Parser[WhereEval] =
    ( simplePredicate ~ setOp ~ where ^^ { case sp ~ op ~ rp => CompoundWhereEval(sp, op, rp) }
    | simplePredicate
  )

  def simplePredicate: Parser[WhereEval] =
    ( ident ~ "is" ~ "null" ^^ { case i ~ is ~ nul => IsNullEval(i, true) }
    | ident ~ "is" ~ "not" ~ "null" ^^ { case i ~ is ~ no ~ nul => IsNullEval(i, false) }
    | ident ~ "between" ~ stringLit ~ "and" ~ stringLit ^^ { case i ~ bw ~ a ~ an ~ b => IsBetweenEval(i, a, b) }
    | ident ~ "in" ~ "(" ~ repsep(stringLit, ",") ~ ")" ^^ { case i ~ instr ~ lp ~ strs ~ rp => InEval(i, strs) }
    | "every" ~ "(" ~ ident ~ ")" ~ op ~ stringLit ^^ { case estr ~ lp ~ i ~ rp ~ o ~ v => EveryEval(i, o, v)}
    | ident ~ op ~ stringLit ^^ { case i ~ o ~ v => SimpleWhereEval(i, o, v) }
    | "(" ~> where <~ ")"
  )
  def setOp = "and" | "or" | "intersection"
  def op = "=" | "!=" | ">" | "<" | ">=" | "<=" | "like" | "not" ~ "like" ^^ { case n ~ l => "not-like" }

  def outputList: Parser[OutputEval] =
    ( "*" ^^^ { new AllOutput }
    | "count(*)" ^^^ { new CountOutput }
    | repsep(ident, ",") ^^ { attrNames => CompoundOutput(attrNames) }
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
