package fn10.server;

import java.io.IOException;
import java.util.Scanner;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

public class App {
    public static void main(String[] args) throws IOException {
        Server server = new Server(443);

        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);

        JakartaWebSocketServletContainerInitializer.configure(handler, null);
        handler.addServlet(JWSInitServlet.class, "/ws/*");

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
