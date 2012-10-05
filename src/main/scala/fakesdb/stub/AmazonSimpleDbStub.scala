package fakesdb.stub

import fakesdb.actions.ItemUpdates
import fakesdb.SelectParser
import com.amazonaws.services.simpledb.AmazonSimpleDB
import com.amazonaws.services.simpledb.model.UpdateCondition
import com.amazonaws.services.simpledb.model.Attribute
import com.amazonaws.services.simpledb.model.Item
import com.amazonaws.services.simpledb.model.SelectRequest
import com.amazonaws.services.simpledb.model.ListDomainsRequest
import com.amazonaws.services.simpledb.model.GetAttributesRequest
import com.amazonaws.services.simpledb.model.PutAttributesRequest
import com.amazonaws.services.simpledb.model.CreateDomainRequest
import com.amazonaws.services.simpledb.model.SelectResult
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest
import com.amazonaws.services.simpledb.model.DeleteDomainRequest
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest
import com.amazonaws.services.simpledb.model.GetAttributesResult
import com.amazonaws.services.simpledb.model.DomainMetadataResult
import com.amazonaws.services.simpledb.model.DomainMetadataRequest
import com.amazonaws.services.simpledb.model.ListDomainsResult
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest
import scala.collection.JavaConversions._

/** Stubs out the {@link AmazonSimpleDB} interface in memory. */
class AmazonSimpleDbStub extends AmazonSimpleDB {

  private val data = new fakesdb.Data()
  
  def flush() {
    data.flush
  }

  def dump() {
    data.getDomains.foreach { domain =>
      domain.getItems.foreach { item =>
        item.getAttributes.foreach { attribute =>
          println(domain.name + " " + item.name + " " + attribute.name + " " + attribute.values.mkString(", "))
        }
      }
    }
  }

  override def setEndpoint(endpoint: String): Unit = {
    // noop
  }

  override def select(req: SelectRequest): SelectResult = { 
    val se = SelectParser.makeSelectEval(req.getSelectExpression)
    val result = se.select(data, Option(req.getNextToken).map(_.toInt))
    val nextToken = if (result._3) result._2.toString else null
    return new SelectResult()
      .withNextToken(nextToken)
      .withItems(result._1.map { i => new Item(i._1, i._2.map(a => new Attribute(a._1, a._2))) } )
  }

  override def deleteAttributes(req: DeleteAttributesRequest): Unit = {
    for (c <- toConditional(req.getExpected)) {
      data.getDomain(req.getDomainName).get.getItem(req.getItemName).get.assertCondition(c)
    }
    val updates = new ItemUpdates()
    for (attribute <- req.getAttributes) {
      updates.add(req.getItemName, attribute.getName)
    }
    updates.delete(data.getDomain(req.getDomainName).get)
  }

  override def putAttributes(req: PutAttributesRequest): Unit = {
    for (c <- toConditional(req.getExpected)) {
      data.getDomain(req.getDomainName).get.getItem(req.getItemName).get.assertCondition(c)
    }
    val updates = new ItemUpdates()
    for (attribute <- req.getAttributes) {
      updates.add(req.getItemName, attribute.getName, attribute.getValue, attribute.getReplace)
    }
    updates.update(data.getDomain(req.getDomainName).get)
  }

  override def batchDeleteAttributes(req: BatchDeleteAttributesRequest): Unit = {
    val updates = new ItemUpdates()
    for (item <- req.getItems) {
      for (attribute <- item.getAttributes) {
        updates.add(item.getName, attribute.getName)
      }
    }
    updates.delete(data.getDomain(req.getDomainName).get)
  }

  override def batchPutAttributes(req: BatchPutAttributesRequest): Unit = {
    val updates = new ItemUpdates()
    for (item <- req.getItems) {
      for (attribute <- item.getAttributes) {
        updates.add(item.getName, attribute.getName, attribute.getValue, attribute.getReplace)
      }
    }
    updates.update(data.getDomain(req.getDomainName).get)
  }

  override def deleteDomain(req: DeleteDomainRequest): Unit = {
    data.deleteDomain(data.getDomain(req.getDomainName).get)
  }

  override def createDomain(req: CreateDomainRequest): Unit = {
    data.getOrCreateDomain(req.getDomainName)
  }

  override def listDomains(): ListDomainsResult = { 
    new ListDomainsResult().withDomainNames(data.getDomains.map { _.name }.toList)
  }

  override def listDomains(req: ListDomainsRequest): ListDomainsResult = { 
    new ListDomainsResult().withDomainNames(data.getDomains.map { _.name }.toList)
  }

  override def getAttributes(req: GetAttributesRequest): GetAttributesResult = { 
    data.getDomain(req.getDomainName).get.getItem(req.getItemName) match {
      case Some(item) => new GetAttributesResult().withAttributes(item.getAttributes
        .filter(a => req.getAttributeNames.size == 0 || req.getAttributeNames.contains(a.name))
        .flatMap(a => a.getValues.map(v => new Attribute(a.name, v))).toList)
      case None => new GetAttributesResult()
    }
  }

  override def domainMetadata(req: DomainMetadataRequest): DomainMetadataResult = {
    val domain = data.getDomain(req.getDomainName).get
    return new DomainMetadataResult()
      .withItemCount(domain.getItems.size)
      .withAttributeNameCount(domain.getItems.foldLeft(0)(_ + _.getAttributes.size))
      .withAttributeValueCount(domain.getItems.foldLeft(0)(_ + _.getAttributes.foldLeft(0)(_ + _.getValues.size)))
  }
  
  private def toConditional(condition: UpdateCondition): Option[Tuple2[String, Option[String]]] = {
    if (condition == null) {
      None
    } else if (condition.getExists != null && !condition.getExists) {
      Some((condition.getName, None))
    } else if (condition.getValue != null) {
      Some((condition.getName, Some(condition.getValue)))
    } else if (condition.getName != null) {
      sys.error("Expected value or false exists")
    } else {
      None
    }
  }

}