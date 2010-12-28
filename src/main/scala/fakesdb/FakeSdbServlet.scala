package fakesdb

import javax.servlet.http._
import fakesdb.actions._

class FakeSdbServlet extends HttpServlet {

  val data = new Data

  override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = synchronized {
    val params = Params(request)
    if (!params.contains("Action")) {
      response.setStatus(404)
      return
    }

    var xml = ""
    try {
      val action = params("Action") match {
        case "CreateDomain" => new CreateDomain(data)
        case "DeleteDomain" => new DeleteDomain(data)
        case "DomainMetadata" => new DomainMetadata(data)
        case "ListDomains" => new ListDomains(data)
        case "GetAttributes" => new GetAttributes(data)
        case "PutAttributes" => new PutAttributes(data)
        case "BatchPutAttributes" => new BatchPutAttributes(data)
        case "DeleteAttributes" => new DeleteAttributes(data)
        case "Select" => new Select(data)
        case other => throw new InvalidActionException(other)
      }
      xml = action.handle(params).toString
    } catch {
      case e => {
        xml = toXML(e).toString
        response.setStatus(400)
      }
    }

    response.setContentType("text/xml")
    response.getWriter.write(xml)
  }
  
  private def toXML(t: Throwable) = {
    val code = t match {
      case se: SDBException => se.code
      case _ => "InternalError"
    }
    
    <Response>
      <Errors><Error><Code>{code}</Code><Message>{t.getMessage}</Message><BoxUsage>0</BoxUsage></Error></Errors>
      <RequestId>0</RequestId>
    </Response>    
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse): Unit = doGet(request, response)

  class InvalidActionException(action: String)
    extends SDBException("InvalidAction", "The action %s is not valid for this web service.".format(action))
}
