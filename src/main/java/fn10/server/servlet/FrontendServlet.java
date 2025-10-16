package fn10.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import fn10.server.App;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontendServlet extends HttpServlet {

    private InputStream getWebsiteFileStream(String file) throws IOException {
        Path path = Path.of(App.path, "website", file);
        System.out.println("(" + getClass().getSimpleName() + ") " + "Opening stream: " + path.toFile());
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        InputStream stream;
        if (!request.getRequestURI().contains(".")) {
            response.setContentType("text/html");
            if (!request.getRequestURI().equals("/")) {
                // request.getRequestURI() already has a / at the start
                stream = getWebsiteFileStream(request.getRequestURI() + ".html");
                if (stream != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    stream = getWebsiteFileStream("/404.html");
                }
            } else {
                stream = getWebsiteFileStream("/index.html");
                if (stream != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else {
            response.setContentType(request.getContentType() == null ? getContentTypeFromURL(request.getRequestURI())
                    : request.getContentType());
            // request.getRequestURI() already has a / at the start
            stream = getWebsiteFileStream(request.getRequestURI());
            if (stream != null) {
                response.setStatus(HttpServletResponse.SC_OK);

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        if (stream != null) {
            byte[] buffer = new byte[1024 * 8];
            int j = -1;
            while ((j = stream.read(buffer)) != -1) {
                response.getWriter().write(new String(buffer, StandardCharsets.ISO_8859_1), 0, j);
            }
            stream.close();
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
