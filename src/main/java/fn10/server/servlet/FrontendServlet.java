package fn10.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontendServlet extends HttpServlet {
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
                
        InputStream stream;
        if (!request.getRequestURI().contains(".")) {
            response.setContentType("text/html");
            if (!request.getRequestURI().equals("/")) {
                // request.getRequestURI() already has a / at the start
                stream = getClass().getResourceAsStream(request.getRequestURI() + ".html");
                if (stream != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    stream = getClass().getResourceAsStream("/404.html");
                }
            } else {
                stream = getClass().getResourceAsStream("/index.html");
                if (stream != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    stream = getClass().getResourceAsStream("/404.html");
                }
            }
        } else {
            response.setContentType(request.getContentType() == null ? getContentTypeFromURL(request.getRequestURI())
                    : request.getContentType());
            // request.getRequestURI() already has a / at the start
            stream = getClass().getResourceAsStream(request.getRequestURI());
            if (stream != null) {
                response.setStatus(HttpServletResponse.SC_OK);

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        byte[] buffer = new byte[1024 * 8];
        int j = -1;
        while ((j = stream.read(buffer)) != -1) {
            response.getWriter().write(new String(buffer, StandardCharsets.ISO_8859_1), 0, j);
        }

        System.out.println("(" + getClass().getSimpleName() + ") " + request.getMethod() + " ("
                + response.getContentType() + "): " + request.getRequestURI());

    }

    public String getContentTypeFromURL(String requestURI) {
        String extension = requestURI.split("\\.")[1];
        switch (extension) {
            case "js":
                return "text/javascript";

            case "png":
                return "image/png";

            default:
                return "application/octet-stream";
        }
    }
}
