package fn10.server.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicNameValuePair;
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

            case "getChatByName":
                if (UriParts[2].isEmpty() || UriParts[2] == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                Chat chat = Chat.getChatByName(URLDecoder.decode(UriParts[2], StandardCharsets.UTF_8));
                if (chat == null) {
                    System.out.println("Cannot find chat with name \"" + UriParts[2] + '"');
                    response.setStatus(HttpStatus.NOT_FOUND_404);
                }
                response.getWriter().println(chat.getId());
                break;

            case "isPersonInChat":
                // format: api/isPersonInChat/(chat #)/(username)
                if (UriParts[2].isEmpty() || UriParts[2] == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                if (UriParts[3].isEmpty() || UriParts[3] == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                Chat chat2 = Chat
                        .getPublicChatById(Integer.parseInt(URLDecoder.decode(UriParts[2], StandardCharsets.UTF_8)));
                response.setContentType("text/plain");
                response.getWriter().print(
                        chat2.userIsInChat(URLDecoder.decode(UriParts[3], StandardCharsets.UTF_8)));
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
                    String clientKey = new String(request.getInputStream().readAllBytes()).trim();
                    if (clientKey.isBlank()) {
                        response.setStatus(HttpStatus.BAD_REQUEST_400);
                        break;
                    }

                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost post = new HttpPost("https://hcaptcha.com/siteverify");

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("secret",
                            Files.readString(Path.of(App.path, "secretCapchaKey.txt")).trim()));
                    params.add(new BasicNameValuePair("response",
                            clientKey));

                    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    post.setHeader("Accept", "application/json");

                    post.setEntity(new UrlEncodedFormEntity(params));

                    System.out.println("(" + getClass().getSimpleName() + ") "
                            + "Sending POST to https://hcaptcha.com/siteverify. Headers: ");
                    for (Header header : post.getHeaders()) {
                        System.out.println(header.getName() + ": " + header.getValue());
                    }
                    // + new String(post.getEntity().getContent().readAllBytes()));

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
