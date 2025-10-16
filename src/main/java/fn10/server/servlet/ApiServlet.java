package fn10.server.servlet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fn10.server.App;
import fn10.server.chat.Chat;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiServlet extends HttpServlet {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI().replace("/api", "");
        String[] UriParts = requestURI.split("/");

        switch (UriParts[1]) {
            case "getRoomsAndPeopleInThem":
                response.setContentType("text/json");

                response.getWriter().println(gson.toJson(Chat.getChatIdsAndPeopleInThem()));
                break;

            case "capchaValid":

                /*
                 * if (UriParts.length > 1 && !UriParts[1].isEmpty()) {
                 * 
                 * } else {
                 * response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
                 * response.getWriter().println("Missing ");
                 * }
                 */
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);

                break;

            default:
                response.setStatus(HttpStatus.NOT_FOUND_404);
                break;
        }

        System.out.println("(" + getClass().getSimpleName() + ") " + request.getMethod() + " ("
                + request.getContentType() + "): " + request.getRequestURI());
    }

    private static class CapchaVerifyRequest {
        @SuppressWarnings("unused")
        public String secret;
        @SuppressWarnings("unused")
        public String response;

        public CapchaVerifyRequest(String s, String re) {
            this.secret = s;
            this.response = re;
            /*if (mote != null)
                if (mote.isBlank())
                    this.remoteip = mote;*/ //lets not send the ip of everyone to google :)
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI().replace("/api", "");
        String[] UriParts = requestURI.split("/");

        System.out.println(request.getRemoteAddr());

        switch (UriParts[1]) {
            case "getRoomsAndPeopleInThem":
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
                /*
                 * response.setContentType("text/json");
                 * 
                 * response.getWriter().println(gson.toJson(Chat.getChatIdsAndPeopleInThem()));
                 */
                break;

            case "capchaValid":
                try {
                    String clientKey = new String(request.getInputStream().readAllBytes());
                    if (clientKey.isBlank()) {
                        response.setStatus(HttpStatus.BAD_REQUEST_400);
                        break;
                    }

                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost post = new HttpPost("https://www.google.com/recaptcha/api/siteverify");

                    

                    post.setEntity(EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON)
                            .setText(gson.toJson(
                                    new CapchaVerifyRequest(Files.readString(Path.of(App.path, "secretCapchaKey.txt")),
                                            clientKey)))
                            .build());

                    System.out.println("(" + getClass().getSimpleName() + ") "
                            + "Sending POST to https://www.google.com/recaptcha/api/siteverify, with params: "
                            + new String(post.getEntity().getContent().readAllBytes()));

                    response.setContentType("text/plain");
                    response.getWriter().println(client.execute(post, new HttpClientResponseHandler<String>() {

                        @Override
                        public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                            HttpEntity entity = response.getEntity();
                            String res = new String(entity.getContent().readAllBytes());
                            System.out.println("(" + getClass().getSimpleName() + ") "
                                    + "Got responce from recaptcha: " + res);
                            return res;
                        }

                    }));

                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                }

                // response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);

                break;

            default:
                response.setStatus(HttpStatus.NOT_FOUND_404);
                break;
        }

        System.out.println("(" + getClass().getSimpleName() + ") " + request.getMethod() + " ("
                + request.getContentType() + "): " + request.getRequestURI());
    }
}
