package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class DomainMetadata(data: Data) extends Action(data) {
  
  def handle(params: Params): NodeSeq = {
    def sum(list: List[Int]) = list.foldLeft(0)(_ + _)
    val allItems = data.getDomains.flatMap(_.getItems).toList
    val allAttrs = allItems.flatMap(_.getAttributes.toList)
    val allValues = allAttrs.flatMap(_.getValues.toList)
    <DomainMetadataResponse xmlns={namespace}>
      <DomainMetadataResult>
        <ItemCount>{allItems.size}</ItemCount>
        <ItemNamesSizeBytes>{sum(allItems.map(_.name.size))}</ItemNamesSizeBytes>
        <AttributeNameCount>{allAttrs.toList.size}</AttributeNameCount>
        <AttributeNamesSizeBytes>{sum(allAttrs.map(_.name.size))}</AttributeNamesSizeBytes>
        <AttributeValueCount>{allValues.size}</AttributeValueCount>
        <AttributeValuesSizeBytes>{sum(allValues.map(_.size))}</AttributeValuesSizeBytes>
        <Timestamp>0</Timestamp>
      </DomainMetadataResult>
      {responseMetaData}
    </DomainMetadataResponse>
  }

}
