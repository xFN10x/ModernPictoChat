package fn10.server;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontendServlet extends HttpServlet {
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        if (!request.getRequestURI().equals("/")) {
            response.getWriter().println(
                    new String(getClass().getResourceAsStream(request.getRequestURI() + ".html").readAllBytes()));
        } else {
        response.getWriter().println(new String(getClass().getResourceAsStream("index.html").readAllBytes()));

        }

        System.out.println(request.getRequestURI());
    }
}
