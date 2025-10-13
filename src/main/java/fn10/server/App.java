package fn10.server;

import java.io.IOException;
import java.util.Scanner;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

public class App {
    public static void main(String[] args) throws IOException {
        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(80);

        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);

        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStorePath("C:\\Users\\mathd\\Downloads\\cert.jks");
        sslContextFactory.setKeyStorePassword("isaplate");
        sslContextFactory.setKeyManagerPassword("isaplate");

        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https));
        sslConnector.setPort(443);


        JakartaWebSocketServletContainerInitializer.configure(handler, null);
        handler.addServlet(JWSInitServlet.class, "/ws/*");
        handler.addServlet(FrontendServlet.class, "/");

        server.setConnectors(new Connector[] { sslConnector, connector });

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
