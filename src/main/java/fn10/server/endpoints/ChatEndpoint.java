package fn10.server.endpoints;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fn10.server.chat.Chat;
import fn10.server.chat.Chat.ChatMessage;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class ChatEndpoint {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class Message {
        public static final int ENTER_ROOM = 0;
        public static final int SEND_MESSAGE = 1;
        public int messageType;
        public String data;
    }

    private static class JoiningChatData {
        public int roomID;
        public String username;
    }

    private static Set<Session> currentClients = Collections.synchronizedSet(new HashSet<Session>());

    public static void notifySessionsOfMessage(Session exclude, ChatMessage mes) {
        try {
            for (Session session : currentClients) {
                if (Chat.getChatPersonIsIn(session) != null && session != exclude) {
                    session.getBasicRemote().sendText("message got: " + mes.data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void clientConnected(Session session, EndpointConfig config) {
        System.out.println("New Client connected: " + session.getId());

        currentClients.add(session);
    }

    @OnMessage
    public void messageGot(String message, Session session) {
        System.out.println("Got message: " + message);
        if (message.equals("ping")) {
            System.out.println("Got Ping from " + session.getId());
            try {
                session.getBasicRemote().sendText("pong");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        } else if (message.equals("close")) {
            clientLeft(session,
                    new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "The user reloaded the page/left the page."));
            return;
        }
        Message got;
        try {
            got = gson.fromJson(message, Message.class);
        } catch (Exception e) {
            System.out.println("not json message");
            try {
                session.getBasicRemote().sendText("error");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return;
        }

        switch (got.messageType) {
            case Message.ENTER_ROOM:
                JoiningChatData data;
                try {
                    data = gson.fromJson(got.data, JoiningChatData.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        session.getBasicRemote().sendText("Bad Request");
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    break;
                }
                Chat.getPublicChatById(data.roomID).addPerson(data.username, session);
                try {
                    session.getBasicRemote().sendText("{\"status\": \"Connected to chat.\"}");
                    for (Session sessions : currentClients) {
                        try {
                            sessions.getBasicRemote().sendText("reloadChats");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Message.SEND_MESSAGE:
                Chat in = Chat.getChatPersonIsIn(session);
                System.out.println(in);
                if (in != null)
                    in.sendMessage(session, got.data);
                else {
                    try {
                        session.getBasicRemote().sendText("{\"status\": \"Not in chat\"}");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                System.out.println("Cannot find that message type");
                break;
        }
    }

    @OnClose
    public void clientLeft(Session session, CloseReason cr) {
        currentClients.remove(session);
        Chat chat;
        if ((chat = Chat.getChatPersonIsIn(session)) != null) {
            chat.removePerson(session);
        }
        System.out.println("Client disconnected: " + session.getId());
    }
}
