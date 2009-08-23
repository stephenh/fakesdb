package fakesdb;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;

public class Jetty {

  private static final Server SERVER = new Server();

  public static void main(String[] args) {
    // Use SocketConnector because SelectChannelConnector locks files
    Connector connector = new SocketConnector();
    connector.setPort(new Integer(System.getProperty("port", "8080")));
    connector.setMaxIdleTime(60000);

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(FakeSdbServlet.class.getName(), "/*");

    Jetty.SERVER.setConnectors(new Connector[] { connector });
    Jetty.SERVER.setHandlers(new Handler[] { handler });
    Jetty.SERVER.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
    Jetty.SERVER.setStopAtShutdown(true);

    try {
      Jetty.SERVER.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
