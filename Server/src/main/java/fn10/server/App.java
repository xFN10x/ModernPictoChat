package fn10.server;

import java.io.IOException;
import java.util.Scanner;

import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.server.Server;

public class App {
    public static void main(String[] args) throws IOException {
        Server server = new Server(
                "localhost",
                443,
                "/ws",
                null,
                ChatEndpoint.class);
                
        try {
            server.start();

            System.out.println("Server started");

            System.out.println("Press enter to stop.");
            new Scanner(System.in).nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
            System.out.println("Server stopped.");
        }

    }
}
