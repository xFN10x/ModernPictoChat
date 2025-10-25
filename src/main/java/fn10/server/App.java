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

import fn10.server.servlet.ApiServlet;
import fn10.server.servlet.FrontendServlet;
import fn10.server.servlet.WebsocketServlet;

public class App {
    public static String path;

    private static transient boolean SSL = true;

    public static void main(String[] args) throws IOException {
        System.out.println("Enter address of secrets.");
        System.out.print("Directory (enter 1 or 2 to use presets): ");

        Scanner scanner = new Scanner(System.in);
        String nl;
        if (args.length >= 1) {
            nl = args[0];
        } else {
            nl = scanner.nextLine();
        }
        if (nl.equals("1")) {
            path = "C:\\MPC\\";
        } else if (nl.equals("2")) {
            path = "/var/opt/mpc/";
        } else {
            path = nl;
        }
        System.out.print("Port (HTTPS): ");
        int nl2;
        if (args.length >= 2) {
            nl2 = Integer.parseInt(args[1]);
        } else {
            nl2 = Integer.parseInt(scanner.nextLine());
        }
        System.out.print("SSL? (true/false): ");
        String nl3;
        if (args.length >= 3) {
            nl3 = args[2];
        } else {
            nl3 = scanner.nextLine();
        }

        SSL = Boolean.parseBoolean(nl3);
        
        if (SSL) {
            System.out.println("Starting server on https://127.0.0.1:" + nl2);
            System.out.println("(Non-SSL HTTP started at http://127.0.0.1:" + ((nl2) + 1) + ")");
        } else
            System.out.println("Starting server on http://127.0.0.1:" + ((nl2) + 1));

        scanner.close();

        Server server = new Server();

        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);

        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = null;
        if (SSL) {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

            sslContextFactory.setKeyStorePath(path + "cert.jks");
            sslContextFactory.setKeyStorePassword("isaplate");
            sslContextFactory.setKeyManagerPassword("isaplate");

            sslConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(https));
            sslConnector.setPort(nl2);
        }

        ServerConnector connector = new ServerConnector(server,
                new HttpConnectionFactory(https));
        connector.setPort(nl2 + 1);

        JakartaWebSocketServletContainerInitializer.configure(handler, null);
        handler.addServlet(WebsocketServlet.class, "/ws");
        handler.addServlet(FrontendServlet.class, "/");
        handler.addServlet(ApiServlet.class, "/api/*");

        if (SSL)
            server.setConnectors(new Connector[] { sslConnector, connector });
        else
            server.setConnectors(new Connector[] { connector });

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
