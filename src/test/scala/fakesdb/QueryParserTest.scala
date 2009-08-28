package fakesdb

import junit.framework.TestCase
import junit.framework.Assert._

class QueryParserTest extends TestCase {

  var item: Item = null
  var item2: Item = null

  override def setUp(): Unit = {
    item = new Item("item1")
    item2 = new Item("item2")
  }

  def testEquals(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1']")
    item.put("attribute1", "value1", true)
    assertIn(qe, item)

    item.put("attribute1", "value2", true)
    assertNotIn(qe,item)
  }

  def testNotEquals(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' != 'value1']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 that != value1 means true
    item.put("attribute1", "value2", true)
    assertIn(qe, item)
    // Another attribute1 that is value1 still returns true because we found value2
    item.put("attribute1", "value1", false)
    assertIn(qe, item)
  }

  def testLessThan(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' < '001']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 over 001 means false
    item.put("attribute1", "002", true)
    assertNotIn(qe, item)
    // One attribute1 under 001 means true
    item.put("attribute1", "000", false)
    assertIn(qe, item)
  }

  def testGreaterThan(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '001']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 under 001 means false
    item.put("attribute1", "000", true)
    assertNotIn(qe, item)
    // One attribute1 over 001 means true
    item.put("attribute1", "002", false)
    assertIn(qe, item)
  }

  def testLessThanOrEqualTo(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' <= '001']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 over 001 means false
    item.put("attribute1", "002", true)
    assertNotIn(qe, item)
    // One attribute1 equal 001 means true
    item.put("attribute1", "001", false)
    assertIn(qe, item)
  }

  def testGreaterThanOrEqualTo(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' >= '001']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 under 001 means false
    item.put("attribute1", "000", true)
    assertNotIn(qe, item)
    // One attribute1 equal 001 means true
    item.put("attribute1", "001", false)
    assertIn(qe, item)
  }

  def testStartsWith(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' starts-with 'a']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 with b means false
    item.put("attribute1", "b", true)
    assertNotIn(qe, item)
    // One attribute1 with a means true
    item.put("attribute1", "a", false)
    assertIn(qe, item)
  }

  def testDoesNotStartWith(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' does-not-start-with 'a']")
    // No attribute1 at all means false
    assertNotIn(qe, item)
    // One attribute1 with a means false
    item.put("attribute1", "a", true)
    assertNotIn(qe, item)
    // One attribute1 with not-a means true
    item.put("attribute1", "b", false)
    assertIn(qe, item)
  }

  def testAnd(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '1' and 'attribute1' < '5']")
    // No attribute1 or attribute2 at all means false
    assertNotIn(qe, item)
    // Correct attribute1
    item.put("attribute1", "2", true)
    assertIn(qe, item)
    // Outside attribute1
    item.put("attribute1", "6", true)
    assertNotIn(qe, item)
  }

  def testAndNamesMustMatch(): Unit = {
    try {
      val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' and 'attribute2' = 'value2']")
      qe.eval(List())
      fail()
    } catch {
      case e => assertEquals("Attribute comparison names do not match", e.getMessage)
    }
  }

  def testOr(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' < '5' or 'attribute1' > '7']")
    // No attribute1 means false
    assertNotIn(qe, item)
    // Above range matches
    item.put("attribute1", "8", true)
    assertIn(qe, item)
    // Not in range is false
    item.put("attribute1", "7", true)
    assertNotIn(qe, item)
  }
  
  def testNot(): Unit = {
    val qe = QueryParser.makeQueryEval("not ['attribute1' = 'value1']")
    // No attribute1 at all matches
    assertIn(qe, item)
    // Wrong attribute1 matches
    item.put("attribute1", "value2", true)
    assertIn(qe, item)
    // Right attribute1 means false
    item.put("attribute1", "value1", true)
    assertNotIn(qe, item)
  }

  def testCrazyAnd(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' and 'attribute1' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute1", "value2", false)
    // False because value1 is evaled and failed then value2 is evaled and failed
    assertNotIn(qe, item)
  }

  def testIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] intersection ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    item2.put("attribute2", "value2", true)
    assertIn(qe, item)
    assertNotIn(qe, item2)
  }

  def testPrecendence(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] intersection ['attribute2' = 'value2'] union ['attribute3' = 'value3']")
    // could be ((a && b) || c) <-- this one is right
    // or       (a && (b || c))
    // Need a case where a=false but c=true
    item.put("attribute1", "wrong", true)
    item.put("attribute3", "value3", true)
    assertIn(qe, item)
  }

  def testNotBindsBeforeIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("not ['attribute1' = 'value1'] intersection ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    item2.put("attribute1", "value2", true)
    item2.put("attribute2", "value2", true)
    assertNotIn(qe, item)
    assertIn(qe, item2)
  }

  def testUnion(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] union ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item2.put("attribute2", "value2", true)
    assertIn(qe, item)
    assertIn(qe, item2)
  }

  def testSort(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute1'")
    item.put("attribute1", "2", true)
    item2.put("attribute1", "1", true)
    assertIn(qe, item)
    assertIn(qe, item2)
    val sorted = qe.eval(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }

  def testSortDesc(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute1' desc")
    item.put("attribute1", "1", true)
    item2.put("attribute1", "2", true)
    assertIn(qe, item)
    assertIn(qe, item2)
    val sorted = qe.eval(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }

  def testSortAsc(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute1' asc")
    item.put("attribute1", "2", true)
    item2.put("attribute1", "1", true)
    assertIn(qe, item)
    assertIn(qe, item2)
    val sorted = qe.eval(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }
  
  def testSortIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] intersection ['attribute2' > '0'] sort 'attribute1' asc")
    item.put("attribute1", "2", true)
    item.put("attribute2", "2", true)
    item2.put("attribute1", "1", true)
    item2.put("attribute2", "1", true)
    assertIn(qe, item)
    assertIn(qe, item2)
    val sorted = qe.eval(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }

  def testSortFailedIfOtherNonQueriedAttribute(): Unit = {
    try {
      val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute2'")
      qe.eval(List())
      fail()
    } catch {
      case e => assertEquals("Invalid sort attribute attribute2", e.getMessage)
    }
  }

  def testSortFailedIfQueriedAttributeWasNegated(): Unit = {
    try {
      val qe = QueryParser.makeQueryEval("not ['attribute1' > '0'] sort 'attribute1'")
      qe.eval(List())
      fail()
    } catch {
      case e => assertEquals("Invalid sort attribute attribute1", e.getMessage)
    }
  }

  def testKeyWithUnderscores(): Unit = {
    val qe = QueryParser.makeQueryEval("['foo_bar' = 'value1']")
    item.put("foo_bar", "value1", true)
    assertIn(qe, item)
  }

  private def assertIn(qe: QueryEval, i: Item) = {
    val output = qe.eval(List(item, item2))
    assertTrue(output.contains(i))
  }

  private def assertNotIn(qe: QueryEval, i: Item) = {
    val output = qe.eval(List(item, item2))
    assertFalse(output.contains(i))
  }
}

