package fakesdb

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletContextHandler

class Jetty(val server: Server)

object Jetty {
  def apply(port: Int): Jetty = {
    val server = new Server
    // Use SocketConnector because SelectChannelConnector locks files
    val connector = new SocketConnector
    connector.setPort(port)
    connector.setMaxIdleTime(60000)
    connector.setRequestBufferSize(24 * 1024)

    val handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
    handler.addServlet(classOf[FakeSdbServlet].getName(), "/*")

    server.setConnectors(Array(connector))
    server.setHandler(handler)
    server.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
    server.setStopAtShutdown(true);

    new Jetty(server)
  }

  def main(args: Array[String]): Unit = {
    val port = System.getProperty("port", "8080").toInt
    Jetty(port).server.start()
  }
}
