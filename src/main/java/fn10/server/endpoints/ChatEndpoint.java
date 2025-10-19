package fn10.server.endpoints;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NameNotFoundException;

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

    private static class SendingChatData {
        public String data;
        public String id;
    }

    private static Set<Session> currentClients = Collections.synchronizedSet(new HashSet<Session>());

    public static void notifySessionsOfMessage(Session exclude, ChatMessage mes) {
        for (Session session : currentClients) {
            if (Chat.getChatPersonIsIn(session) != null && session != exclude) {
                session.getAsyncRemote().sendText("message got: " + mes.data);
            }
        }
    }

    public static void SendTextToSession(Session ses, String text) throws NameNotFoundException, IOException {
        for (Session currentClient : currentClients) {
            if (currentClient == ses) {
                currentClient.getAsyncRemote().sendText(text);
                return;
            }
        }
        throw new NameNotFoundException("The session: " + ses.getId() + " isnt connected.");
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
                session.getAsyncRemote().sendText("pong");
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
            session.getAsyncRemote().sendText("error");
            return;
        }

        switch (got.messageType) {
            case Message.ENTER_ROOM:
                JoiningChatData data;
                try {
                    data = gson.fromJson(got.data, JoiningChatData.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    session.getAsyncRemote().sendText("Bad Request");
                    break;
                }
                Chat.getPublicChatById(data.roomID).addPerson(data.username, session);
                session.getAsyncRemote()
                        .sendText("{\"status\": \"Connected to chat.\", \"id\": \"" + session.getId() + "\"}");
                for (Session sessions : currentClients) {
                    sessions.getAsyncRemote().sendText("reloadChats");
                }
                break;
            case Message.SEND_MESSAGE:
                Chat in = Chat.getChatPersonIsIn(session);
                SendingChatData sendData;
                try {
                    sendData = gson.fromJson(got.data, SendingChatData.class);
                } catch (Exception e) {
                    session.getAsyncRemote().sendText("Bad Request");
                    return;
                }
                // System.out.println(in);
                if (in != null) {
                    // idk why the hell this is using the wrong session for sending, we are just
                    // gonna have to save it client sided
                    in.sendMessage(sendData.id, sendData.data);
                } else {
                    session.getAsyncRemote().sendText("{\"status\": \"Not in chat\"}");
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
        System.out.println("Client (" + session.getId() + ") disconnected, because " + cr.getReasonPhrase() + " ("
                + cr.getCloseCode().toString() + ")");
    }
}
