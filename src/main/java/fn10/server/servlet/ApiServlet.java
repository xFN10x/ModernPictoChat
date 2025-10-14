package fn10.server.servlet;

import java.io.IOException;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiServlet extends HttpServlet {
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI().replace("/api", "");

        switch (requestURI) {
            case "/getRoomsAndPeopleInThem":
                response.setContentType("text/json");

                response.getWriter().println("{}");
                break;
        
            default:
            response.setStatus(HttpStatus.OK_200);
                break;
        }

        System.out.println("(" + getClass().getName() + ") " + request.getMethod() + ": " + requestURI);

    }
}
