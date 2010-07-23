package fakesdb

import org.mortbay.jetty.Connector
import org.mortbay.jetty.Handler
import org.mortbay.jetty.Server
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.servlet.ServletHandler

class Jetty(val server: Server)

object Jetty {
  def apply(port: Int): Jetty = {
    val server = new Server
    // Use SocketConnector because SelectChannelConnector locks files
    val connector = new SocketConnector
    connector.setPort(port)
    connector.setMaxIdleTime(60000)
    connector.setHeaderBufferSize(24 * 1024)

    val handler = new ServletHandler
    handler.addServletWithMapping(classOf[FakeSdbServlet].getName(), "/*")

    server.setConnectors(Array(connector))
    server.setHandlers(Array(handler))
    server.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
    server.setStopAtShutdown(true);

    new Jetty(server)
  }

  def main(args: Array[String]): Unit = {
    val port = System.getProperty("port", "8080").toInt
    Jetty(port).server.start()
  }
}
