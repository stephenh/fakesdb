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
    assertTrue(qe.eval(item))

    item.put("attribute1", "value2", true)
    assertFalse(qe.eval(item))
  }

  def testNotEquals(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' != 'value1']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 that != value1 means true
    item.put("attribute1", "value2", true)
    assertTrue(qe.eval(item))
    // Another attribute1 that is value1 still returns true because we found value2
    item.put("attribute1", "value1", false)
    assertTrue(qe.eval(item))
  }

  def testLessThan(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' < '001']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 over 001 means false
    item.put("attribute1", "002", true)
    assertFalse(qe.eval(item))
    // One attribute1 under 001 means true
    item.put("attribute1", "000", false)
    assertTrue(qe.eval(item))
  }

  def testGreaterThan(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '001']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 under 001 means false
    item.put("attribute1", "000", true)
    assertFalse(qe.eval(item))
    // One attribute1 over 001 means true
    item.put("attribute1", "002", false)
    assertTrue(qe.eval(item))
  }

  def testLessThanOrEqualTo(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' <= '001']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 over 001 means false
    item.put("attribute1", "002", true)
    assertFalse(qe.eval(item))
    // One attribute1 equal 001 means true
    item.put("attribute1", "001", false)
    assertTrue(qe.eval(item))
  }

  def testGreaterThanOrEqualTo(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' >= '001']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 under 001 means false
    item.put("attribute1", "000", true)
    assertFalse(qe.eval(item))
    // One attribute1 equal 001 means true
    item.put("attribute1", "001", false)
    assertTrue(qe.eval(item))
  }

  def testStartsWith(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' starts-with 'a']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 with b means false
    item.put("attribute1", "b", true)
    assertFalse(qe.eval(item))
    // One attribute1 with a means true
    item.put("attribute1", "a", false)
    assertTrue(qe.eval(item))
  }

  def testDoesNotStartWith(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' does-not-start-with 'a']")
    // No attribute1 at all means false
    assertFalse(qe.eval(item))
    // One attribute1 with a means false
    item.put("attribute1", "a", true)
    assertFalse(qe.eval(item))
    // One attribute1 with not-a means true
    item.put("attribute1", "b", false)
    assertTrue(qe.eval(item))
  }

  def testAnd(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' and 'attribute2' = 'value2']")
    // No attribute1 or attribute2 at all means false
    assertFalse(qe.eval(item))
    // Correct attribute1 and no attribute2 means false
    item.put("attribute1", "value1", true)
    assertFalse(qe.eval(item))
    // Wrong attribute2 at all means false
    item.put("attribute2", "value3", true)
    assertFalse(qe.eval(item))
    // Right attribute2 means true
    item.put("attribute2", "value2", false)
    assertTrue(qe.eval(item))
  }

  def testOr(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' or 'attribute2' = 'value2']")
    // No attribute1 or attribute2 at all means false
    assertFalse(qe.eval(item))
    // Wrong attribute1 and no attribute2 means false
    item.put("attribute1", "value3", true)
    assertFalse(qe.eval(item))
    // Right attribute1 and no attribute2 means true
    item.put("attribute1", "value1", false)
    assertTrue(qe.eval(item))
    // Test other side
    val qe2 = QueryParser.makeQueryEval("['attribute1' = 'value4' or 'attribute2' = 'value2']")
    item.put("attribute2", "value2", false)
    assertTrue(qe2.eval(item))
  }
  
  def testNot(): Unit = {
    val qe = QueryParser.makeQueryEval("not ['attribute1' = 'value1']")
    // No attribute1 at all matches
    assertTrue(qe.eval(item))
    // Wrong attribute1 matches
    item.put("attribute1", "value2", true)
    assertTrue(qe.eval(item))
    // Right attribute1 means false
    item.put("attribute1", "value1", true)
    assertFalse(qe.eval(item))
  }

  def testCrazyAnd(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' and 'attribute1' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute1", "value2", false)
    // False because value1 is evaled/failed then value2 is evaled/failed
    assertTrue(qe.eval(item)) // FAIL
  }

  def testIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] intersection ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    item2.put("attribute2", "value2", true)
    assertTrue(qe.eval(item))
    assertFalse(qe.eval(item2))
  }

  def testNotBindsBeforeIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("not ['attribute1' = 'value1'] intersection ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    item2.put("attribute1", "value2", true)
    item2.put("attribute2", "value2", true)
    assertFalse(qe.eval(item))
    assertTrue(qe.eval(item2))
  }

  def testUnion(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] union ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item2.put("attribute2", "value2", true)
    assertTrue(qe.eval(item))
    assertTrue(qe.eval(item2))
  }

  def testSort(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute1'")
    item.put("attribute1", "2", true)
    item2.put("attribute1", "1", true)
    assertTrue(qe.eval(item))
    assertTrue(qe.eval(item2))
    val sorted = qe.sort(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }

  def testSortDesc(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute1' desc")
    item.put("attribute1", "1", true)
    item2.put("attribute1", "2", true)
    assertTrue(qe.eval(item))
    assertTrue(qe.eval(item2))
    val sorted = qe.sort(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }

  def testSortAsc(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute1' asc")
    item.put("attribute1", "2", true)
    item2.put("attribute1", "1", true)
    assertTrue(qe.eval(item))
    assertTrue(qe.eval(item2))
    val sorted = qe.sort(List(item, item2))
    assertEquals(item2, sorted(0))
    assertEquals(item, sorted(1))
  }

  def testKeyWithUnderscores(): Unit = {
    val qe = QueryParser.makeQueryEval("['foo_bar' = 'value1']")
    item.put("foo_bar", "value1", true)
    assertTrue(qe.eval(item))
  }
}
