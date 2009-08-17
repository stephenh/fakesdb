package fakesdb

import junit.framework.Assert._
import junit.framework.TestCase

class SelectParserTest extends TestCase {

  val data = new Data
  var domaina: Domain = null

  override def setUp(): Unit = {
    data.flush
    domaina = data.getOrCreateDomain("domaina")
  }

  def testEmptyFrom(): Unit = {
    val se = SelectParser.makeSelectEval("select * from domaina")
    assertEquals(0, se.select(data).size)
  }

  def testFromWithData(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itema").put("b", "2", true)
    domaina.getOrCreateItem("itemb").put("c", "3", true)
    val results = SelectParser.makeSelectEval("select * from domaina").select(data)
    assertEquals(2, results.size)
    assertEquals(("itema", List(("a", "1"), ("b", "2"))), results(0))
    assertEquals(("itemb", List(("c", "3"))), results(1))
  }
  
  def testFromWithOneAttribute(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itema").put("b", "2", true)
    domaina.getOrCreateItem("itemb").put("a", "3", true)
    // a is in both
    val results = SelectParser.makeSelectEval("select a from domaina").select(data)
    assertEquals(2, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
    assertEquals(("itemb", List(("a", "3"))), results(1))
    // b in in only one
    val results2 = SelectParser.makeSelectEval("select b from domaina").select(data)
    assertEquals(1, results2.size)
    assertEquals(("itema", List(("b", "2"))), results2(0))
  }

  def testFromWithTwoAttributes(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itema").put("b", "2", true)
    domaina.getOrCreateItem("itemb").put("a", "3", true)
    val results = SelectParser.makeSelectEval("select a, b from domaina").select(data)
    assertEquals(2, results.size)
    assertEquals(("itema", List(("a", "1"), ("b", "2"))), results(0))
    assertEquals(("itemb", List(("a", "3"))), results(1))
  }

  def testFromCount(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    val results = SelectParser.makeSelectEval("select count(*) from domaina").select(data)
    assertEquals(1, results.size)
    assertEquals(("domaina", List(("Count", "1"))), results(0))
  }

  def testFromItemName(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    val results = SelectParser.makeSelectEval("select itemName(), a from domaina").select(data)
    assertEquals(1, results.size)
    assertEquals(("itema", List(("itemName()", "itema"), ("a", "1"))), results(0))
  }

  def testWhereEquals(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a = '1'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
  }

  def testWhereNotEquals(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a != '1'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itemb", List(("a", "2"))), results(0))
  }

  def testWhereGreaterThan(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a > '1'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itemb", List(("a", "2"))), results(0))
  }

  def testWhereLessThan(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a < '2'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
  }

  def testWhereGreaterThanOrEqual(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    domaina.getOrCreateItem("itemc").put("a", "3", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a >= '2'").select(data)
    assertEquals(2, results.size)
    assertEquals(("itemb", List(("a", "2"))), results(0))
    assertEquals(("itemc", List(("a", "3"))), results(1))
  }

  def testWhereLessThanOrEqual(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    domaina.getOrCreateItem("itemc").put("a", "3", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a <= '2'").select(data)
    assertEquals(2, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
    assertEquals(("itemb", List(("a", "2"))), results(1))
  }

  def testWhereLike(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a like '1%'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
  }

  def testWhereNotLike(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a not like '1%'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itemb", List(("a", "2"))), results(0))
  }

  def testWhereIsNull(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("b", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a is null").select(data)
    assertEquals(1, results.size)
    assertEquals(("itemb", List(("b", "2"))), results(0))
  }
  
  def testWhereIsNotNull(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("b", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a is not null").select(data)
    assertEquals(1, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
  }

  def testWhereEqualsAnd(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itema").put("b", "2", true)
    domaina.getOrCreateItem("itemb").put("a", "1", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a = '1' and b = '2'").select(data)
    assertEquals(1, results.size)
    assertEquals(("itema", List(("a", "1"), ("b", "2"))), results(0))
  }

  def testWhereEqualsOr(): Unit = {
    domaina.getOrCreateItem("itema").put("a", "1", true)
    domaina.getOrCreateItem("itemb").put("a", "2", true)
    val results = SelectParser.makeSelectEval("select * from domaina where a = '1' or a = '2'").select(data)
    assertEquals(2, results.size)
    assertEquals(("itema", List(("a", "1"))), results(0))
    assertEquals(("itemb", List(("a", "2"))), results(1))
  }

}
