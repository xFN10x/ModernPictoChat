package fn10.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class ChatEndpoint {

    private static Set<Session> currentClients = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void clientConnected(Session session, EndpointConfig config) {
        currentClients.add(session);
        System.out.println("New Client connected: " + session.getId());
    }

    @OnMessage
    public void messageGot(String message, Session session) {
        System.out.println("Got message: " + message);

        try {
            session.getBasicRemote().sendText("got: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
