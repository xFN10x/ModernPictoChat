package fn10.server.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import fn10.server.App;
import fn10.server.chat.Chat;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiServlet extends HttpServlet {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * {
     * "success": true|false, // is the passcode valid, and does it meet security
     * criteria you specified, e.g. sitekey?
     * 
     * "challenge_ts": timestamp, // timestamp of the challenge (ISO format
     * yyyy-MM-dd'T'HH:mm:ssZZ)
     * 
     * "hostname": string, // the hostname of the site where the challenge was
     * passed
     * 
     * "credit": true|false, // optional: deprecated field
     * 
     * "error-codes": [...] // optional: any error codes
     * }
     */
    public static class HCaptchaValidResponce {
        public boolean success;
        private String challenge_ts;
        public String hostname;
        public boolean credit;
        @SerializedName("error-codes")
        public String[] errorCodes;

        public Instant getTimeComplete() {
            return Instant.parse(challenge_ts);
        }

        public String toString() {
            return gson.toJson(this);
        }
    }

    public static HCaptchaValidResponce isCaptchaValid(String clientKey) {
        if (clientKey.isBlank()) {
            HCaptchaValidResponce build = new HCaptchaValidResponce();
            build.success = false;
            build.challenge_ts = Instant.now().toString();
            return build;
        }

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://hcaptcha.com/siteverify");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        try {
            params.add(new BasicNameValuePair("secret",
                    Files.readString(Path.of(App.path, "secretCapchaKey.txt")).trim()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        params.add(new BasicNameValuePair("response",
                clientKey));

        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Accept", "application/json");

        post.setEntity(new UrlEncodedFormEntity(params));

        System.out.println("(STATIC " + ApiServlet.class.getSimpleName() + ") "
                + "Sending POST to https://hcaptcha.com/siteverify. Headers: ");
        for (Header header : post.getHeaders()) {
            System.out.println(header.getName() + ": " + header.getValue());
        }

        try {
            return gson.fromJson(client.execute(post, new HttpClientResponseHandler<String>() {

                @Override
                public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                    HttpEntity entity = response.getEntity();
                    String res = new String(entity.getContent().readAllBytes());
                    System.out.println("(" + getClass().getSimpleName() + ") "
                            + "Got responce from recaptcha: " + res);
                    return res;
                }

            }), HCaptchaValidResponce.class);
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
                Chat chat = Chat.getChat(URLDecoder.decode(UriParts[2], StandardCharsets.UTF_8));
                if (chat == null) {
                    System.out.println("Cannot find chat with name \"" + UriParts[2] + '"');
                    response.setStatus(HttpStatus.NOT_FOUND_404);
                }
                response.getWriter().println(chat.getId());
                break;

            case "getChatById":
                if (UriParts[2].isEmpty() || UriParts[2] == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                Chat chat2 = Chat.getChat(Integer.parseInt(URLDecoder.decode(UriParts[2], StandardCharsets.UTF_8)));
                if (chat2 == null) {
                    System.out.println("Cannot find chat with id \"" + UriParts[2] + '"');
                    response.setStatus(HttpStatus.NOT_FOUND_404);
                }
                response.getWriter().println(chat2.Name);
                break;

            case "isPersonInChat":
                // format: api/isPersonInChat/(chat #)/(username)
                if (UriParts[2].isEmpty() || UriParts[2] == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                if (UriParts[3].isEmpty() || UriParts[3] == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST_400);
                }
                Chat chat3 = Chat
                        .getChat(Integer.parseInt(URLDecoder.decode(UriParts[2], StandardCharsets.UTF_8)));
                response.setContentType("text/plain");
                response.getWriter().print(
                        chat3.userIsInChat(URLDecoder.decode(UriParts[3], StandardCharsets.UTF_8)));
                break;

            case "capchaValid":
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
                response.setStatus(HttpStatus.GONE_410);
                break;

            default:
                response.setStatus(HttpStatus.NOT_FOUND_404);
                break;
        }

        System.out.println("(" + getClass().getSimpleName() + ") " + request.getMethod() + " ("
                + request.getContentType() + "): " + request.getRequestURI());
    }
}
