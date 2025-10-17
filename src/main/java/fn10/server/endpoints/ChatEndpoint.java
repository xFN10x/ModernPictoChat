package fn10.server.endpoints;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
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

    @OnClose
    public void clientLeft(Session session, CloseReason cr) {
        currentClients.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }
}
