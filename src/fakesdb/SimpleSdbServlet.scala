package fakesdb;

import javax.servlet.http._
import fakesdb.actions._

class FakeSdbServlet extends HttpServlet {

  val data: Data = new Data

  override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = synchronized {
    val params = Params(request)
    println("Got params "+params)

    if (!params.contains("Action")) {
      response.setStatus(404)
      return
    }

    val action = params("Action") match {
      case "CreateDomain" => new CreateDomain(data)
      case "DeleteDomain" => new DeleteDomain(data)
      case "ListDomains" => new ListDomains(data)
      case "GetAttributes" => new GetAttributes(data)
      case "PutAttributes" => new PutAttributes(data)
      case "BatchPutAttributes" => new BatchPutAttributes(data)
      case "DeleteAttributes" => new DeleteAttributes(data)
      case "Query" => new Query(data)
      case "QueryWithAttributes" => new QueryWithAttributes(data)
      case other => error("Invalid action "+other)
    }

    val xml = action.handle(params).toString
    println("Returning "+xml)
    response.setContentType("text/xml")
    response.getWriter.write(xml)
  }

}
