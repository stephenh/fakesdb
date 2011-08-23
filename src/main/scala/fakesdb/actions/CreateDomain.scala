package fakesdb.actions

import scala.xml.NodeSeq
import fakesdb._

class CreateDomain(data: Data) extends Action(data) {

  def handle(params: Params): NodeSeq = {
    val domainName = params.getOrElse("DomainName", sys.error("No domain name"))
    if (domainName == "_flush") {
      data.flush() // The special one
    } else if (domainName == "_dump") {
      dump(domainName)
    } else {
      data.getOrCreateDomain(domainName)
    }
    <CreateDomainResponse xmlns={namespace}>
      {responseMetaData}
    </CreateDomainResponse>
  }

  def dump(domainName: String) {
    for (d <- data.getDomains) {
      println("Domain "+d.name)
      for (i <- d.getItems) {
        println("\tItem "+i.name)
        for (a <- i.getAttributes) {
          println("\t\t"+a.name+" = "+a.getValues.mkString(", "))
        }
      }
    }
  }
}
