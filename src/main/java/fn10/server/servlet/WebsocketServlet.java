package fn10.server.servlet;

import java.io.IOException;

import fn10.server.endpoints.ChatEndpoint;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

public class WebsocketServlet extends HttpServlet {
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
            container.addEndpoint(ServerEndpointConfig.Builder.create(ChatEndpoint.class, "/ws").build());
        } catch (DeploymentException x) {
            throw new ServletException(x);
        }
    }

    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("/");
        System.out.println("(" + getClass().getSimpleName() + ") " + request.getMethod() + " ("
                + request.getContentType() + "): " + request.getRequestURI());
    }
}
