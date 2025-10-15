package fn10.server.servlet;

import java.io.IOException;

import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fn10.server.chat.Chat;
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
        String[] UriParts = requestURI.split("/");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        switch (UriParts[0]) {
            case "getRoomsAndPeopleInThem":
                response.setContentType("text/json");

                response.getWriter().println(gson.toJson(Chat.getChatIdsAndPeopleInThem()));
                break;

            case "capchaValid":
            request.get
            break;

            default:
                response.setStatus(HttpStatus.NOT_FOUND_404);
                break;
        }
        
        System.out.println("(" + getClass().getSimpleName() + ") " + request.getMethod() + " ("+ request.getContentType() + "): " + request.getRequestURI());
    }
}
