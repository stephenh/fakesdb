package fakesdb

import org.junit._
import org.junit.Assert._

class QueryParserTest {

  var item: Item = null
  var item2: Item = null

  @Before
  def setUp(): Unit = {
    item = new Item("item1")
    item2 = new Item("item2")
  }

  @Test
  def testEquals(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1']")
    item.put("attribute1", "value1", true)
    assertIn(qe, item)

    item.put("attribute1", "value2", true)
    assertNotIn(qe,item)
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  def testAndNamesMustMatch(): Unit = {
    try {
      val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' and 'attribute2' = 'value2']")
      qe.eval(List())
      fail()
    } catch {
      case e => assertEquals("Attribute comparison names do not match", e.getMessage)
    }
  }

  @Test
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
  
  @Test
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

  @Test
  def testCrazyAnd(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1' and 'attribute1' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute1", "value2", false)
    // False because value1 is evaled and failed then value2 is evaled and failed
    assertNotIn(qe, item)
  }

  @Test
  def testIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] intersection ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    item2.put("attribute2", "value2", true)
    assertIn(qe, item)
    assertNotIn(qe, item2)
  }

  @Test
  def testIntersection2(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value2'] intersection ['attribute2' >= 'value2'] sort 'attribute2'")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    assertNotIn(qe, item)
  }

  @Test
  def testPrecendence(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] intersection ['attribute2' = 'value2'] union ['attribute3' = 'value3']")
    // could be ((a && b) || c) <-- this one is right
    // or       (a && (b || c))
    // Need a case where a=false but c=true
    item.put("attribute1", "wrong", true)
    item.put("attribute3", "value3", true)
    assertIn(qe, item)
  }

  @Test
  def testNotBindsBeforeIntersection(): Unit = {
    val qe = QueryParser.makeQueryEval("not ['attribute1' = 'value1'] intersection ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item.put("attribute2", "value2", true)
    item2.put("attribute1", "value2", true)
    item2.put("attribute2", "value2", true)
    assertNotIn(qe, item)
    assertIn(qe, item2)
  }

  @Test
  def testUnion(): Unit = {
    val qe = QueryParser.makeQueryEval("['attribute1' = 'value1'] union ['attribute2' = 'value2']")
    item.put("attribute1", "value1", true)
    item2.put("attribute2", "value2", true)
    assertIn(qe, item)
    assertIn(qe, item2)
  }

  @Test
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

  @Test
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

  @Test
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
  
  @Test
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

  @Test
  def testSortFailedIfOtherNonQueriedAttribute(): Unit = {
    try {
      val qe = QueryParser.makeQueryEval("['attribute1' > '0'] sort 'attribute2'")
      qe.eval(List())
      fail()
    } catch {
      case e => assertEquals("Invalid sort attribute attribute2", e.getMessage)
    }
  }

  @Test
  def testSortFailedIfQueriedAttributeWasNegated(): Unit = {
    try {
      val qe = QueryParser.makeQueryEval("not ['attribute1' > '0'] sort 'attribute1'")
      qe.eval(List())
      fail()
    } catch {
      case e => assertEquals("Invalid sort attribute attribute1", e.getMessage)
    }
  }

  @Test
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

