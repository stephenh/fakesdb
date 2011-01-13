package fakesdb

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.LinkedHashSet

class Data {
  private val domains = new LinkedHashMap[String, Domain]()
  def getDomains(): Iterator[Domain] = domains.valuesIterator
  def getDomain(name: String): Option[Domain] = domains.get(name)
  def getOrCreateDomain(name: String): Domain = domains.getOrElseUpdate(name, new Domain(name))
  def deleteDomain(domain: Domain): Unit = domains.remove(domain.name)
  def flush(): Unit = domains.clear
}

class Domain(val name: String) {
  private val items = new LinkedHashMap[String, Item]()
  def getItems(): Iterator[Item] = items.valuesIterator
  def getItem(name: String): Option[Item] = items.get(name)
  def getOrCreateItem(name: String): Item = items.getOrElseUpdate(name, new Item(name))
  def deleteIfEmpty(item: Item) = if (!item.getAttributes.hasNext) items.remove(item.name)
  def deleteItem(item: Item) = items.remove(item.name)
}

class Item(val name: String) {
  private val attributes = new LinkedHashMap[String, Attribute]()
  def getAttributes(): Iterator[Attribute] = attributes.valuesIterator
  def getAttribute(name: String): Option[Attribute] = attributes.get(name)
  def getOrCreateAttribute(name: String): Attribute = attributes.getOrElseUpdate(name, new Attribute(name))
  def put(name: String, value: String, replace: Boolean): Unit = {
    this.getOrCreateAttribute(name).put(value, replace)
  }
  def put(name: String, values: Seq[String], replace:Boolean) {
    this.getOrCreateAttribute(name).put(values, replace)
  }
  def delete(name: String): Unit = attributes.remove(name)
  def delete(name: String, value: String): Unit = {
    getAttribute(name) match {
      case Some(a) => a.deleteValues(value) ; removeIfNoValues(a)
      case None =>
    }
  }
  private def removeIfNoValues(attribute: Attribute) = {
    if (attribute.empty) attributes.remove(attribute.name)
  }
}

class Attribute(val name: String) {
  private val values = new HashSet[String]()
  def getValues(): Iterator[String] = values.iterator
  def empty(): Boolean = values.size == 0
  def deleteValues(value: String) = {
    values.remove(value)
  }
  def put(value: String, replace: Boolean) = {
    if (replace) values.clear
    values += value
  }
  
  def put(_values: Seq[String], replace: Boolean) = {
    if (replace) values.clear
    values ++= _values
  }
}
