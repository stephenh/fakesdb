package fakesdb

import java.io.{PrintWriter, StringWriter}
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
        case "BatchDeleteAttributes" => new BatchDeleteAttributes(data)
        case "DeleteAttributes" => new DeleteAttributes(data)
        case "Select" => new Select(data)
        case other => throw new InvalidActionException(other)
      }
      xml = action.handle(params).toString
    } catch {
      case e: Exception => {
        xml = toXML(e).toString
        response.setStatus(e match {
          case se: SDBException => se.httpStatus
          case _ => 400
        })
      }
    }

    response.setContentType("text/xml")
    response.getWriter.write(xml)
  }

  private def toXML(t: Throwable) = {
    val xmlCode = t match {
      case se: SDBException => se.xmlCode
      case _ => "InternalError"
    }

    val stacktrace = new StringWriter()
    t.printStackTrace(new PrintWriter(stacktrace))

    <Response>
      <Errors><Error><Code>{xmlCode}</Code><Message>{t.getClass.getSimpleName}: {t.getMessage}</Message><BoxUsage>0</BoxUsage></Error></Errors>
      <RequestId>0</RequestId>
      <Stacktrace>{stacktrace.toString}</Stacktrace>
    </Response>
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse): Unit = doGet(request, response)

  class InvalidActionException(action: String)
    extends SDBException(400, "InvalidAction", "The action %s is not valid for this web service.".format(action))
}
