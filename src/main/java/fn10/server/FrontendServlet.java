package fn10.server;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontendServlet extends HttpServlet {
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.getRequestURI().contains(".")) {
            response.setContentType("text/html");
            if (!request.getRequestURI().equals("/")) {
                // request.getRequestURI() already has a / at the start
                InputStream stream = getClass().getResourceAsStream(request.getRequestURI() + ".html");
                if (stream != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(new String(stream.readAllBytes()));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    stream = getClass().getResourceAsStream("/404.html");
                    response.getWriter().println(new String(stream.readAllBytes()));
                }
            } else {
                InputStream stream = getClass().getResourceAsStream("/index.html");
                if (stream != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(new String(stream.readAllBytes()));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    stream = getClass().getResourceAsStream("/404.html");
                    response.getWriter().println(new String(stream.readAllBytes()));
                }
            }
        } else if (request.getRequestURI().endsWith(".ts") || request.getRequestURI().endsWith(".js")) {
            response.setContentType("text/javascript");
            // request.getRequestURI() already has a / at the start
            InputStream stream = getClass().getResourceAsStream(request.getRequestURI());
            if (stream != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(new String(stream.readAllBytes()));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.setContentType("application/octet-stream");
            // request.getRequestURI() already has a / at the start
            InputStream stream = getClass().getResourceAsStream(request.getRequestURI());
            if (stream != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(new String(stream.readAllBytes()));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        System.out.println(request.getMethod() + ": " + request.getRequestURI());
    }
}
