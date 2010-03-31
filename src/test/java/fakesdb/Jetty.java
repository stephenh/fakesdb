package fakesdb;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;

public class Jetty {
  private final Server server = new Server();

  public Jetty(final int port) {
    // Use SocketConnector because SelectChannelConnector locks files
    Connector connector = new SocketConnector();
    connector.setPort(port);
    connector.setMaxIdleTime(60000);
    connector.setHeaderBufferSize(24 * 1024);

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(FakeSdbServlet.class.getName(), "/*");

    this.server.setConnectors(new Connector[] { connector });
    this.server.setHandlers(new Handler[] { handler });
    this.server.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
    this.server.setStopAtShutdown(true);
  }

  public Server server() {
    return this.server;
  }

  public static void main(String[] args) {
    try {
      final Integer port = Integer.valueOf(System.getProperty("port", "8080"));
      new Jetty(port).server().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
