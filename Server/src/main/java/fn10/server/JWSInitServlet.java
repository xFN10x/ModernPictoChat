package fn10.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;

public class JWSInitServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        try { // taken from
              // https://jetty.org/docs/jetty/12.1/programming-guide/server/websocket.html
              // (not ai)
              // Retrieve the ServerContainer from the ServletContext attributes.
            ServerContainer container = (ServerContainer) getServletContext()
                    .getAttribute(ServerContainer.class.getName());

            // Configure the ServerContainer.
            container.setDefaultMaxTextMessageBufferSize(128 * 1024);

            // Simple registration of your WebSocket endpoints.
            container.addEndpoint(ChatEndpoint.class);
        } catch (DeploymentException x) {
            throw new ServletException(x);
        }
    }
}
