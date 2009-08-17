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

}
