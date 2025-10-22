package fn10.server.endpoints;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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

    private static final String defaultIcon = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAfQAAAH0BAMAAAA5+MK5AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAeUExURQAAAAMDA+/v7/////T09BAQEPr6+vn5+fX19f7+/sOgV/8AAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAYdEVYdFNvZnR3YXJlAFBhaW50Lk5FVCA1LjEuOWxu2j4AAAC2ZVhJZklJKgAIAAAABQAaAQUAAQAAAEoAAAAbAQUAAQAAAFIAAAAoAQMAAQAAAAIAAAAxAQIAEAAAAFoAAABphwQAAQAAAGoAAAAAAAAAYAAAAAEAAABgAAAAAQAAAFBhaW50Lk5FVCA1LjEuOQADAACQBwAEAAAAMDIzMAGgAwABAAAAAQAAAAWgBAABAAAAlAAAAAAAAAACAAEAAgAEAAAAUjk4AAIABwAEAAAAMDEwMAAAAABMz8BIJY/XoAAAAjpJREFUeNrt20ENQjEQRdFvAQtYqAUsYAELWMACbtmTvMUk/dBMz913pqf7HseSXVL/vhg6Ojo6Ojo6Ojo6Ojo6Ojo6Onrz0NHR0ZuHjo6O3jx0dHT05qGjo6M3Dx0dHb156Ojo6Od3bRI6Ojo6Ojo6eoPQ0dHR0dHR0RuEjo6Ojo6Ojt4gdHR0dHR0dPQGoaOjo6Ojo6M3CB0dHb1KH6k4apT7xSh0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR09O70W+qeeqTifeOoZ+qVQkdHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dH/24sGTo6Ojo6Ojo6Ojo6Ojo6+nqho6Ojo6Ojo6Ojo6Ojo6OvFzo6Ojo6Ojo6Ojo6Ojo6+nqhn0uvV4fEUe8UOjo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ojo6Ovq29Hhi4pvE6svR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0belz1xSrv4zBB0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR19W3p9+8TQ0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dElSZIkSZIkSZIkSZIkSZIkSZJW7gO8gusn2MJ+5wAAAABJRU5ErkJggg==";

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class Message {
        public static final int ENTER_ROOM = 0;
        public static final int SEND_MESSAGE = 1;
        public int messageType;
        public String data;
    }

    private static class JoiningChatData {
        public int roomID;
        public UserInfo user;
    }

    private static class SendingChatData {
        public String data;
        public String id;
    }

    public static class UserInfo {
        public String username;
        public String iconData;
        public int messagesSent;
        public Vector<Byte> rgb;

        public UserInfo(String name, String iconData, int messagesSent, int r, int g, int b) {
            this.username = name;
            this.iconData = iconData;
            this.messagesSent = messagesSent;
            this.rgb = new Vector<Byte>(3);
            rgb.add((byte) r);
            rgb.add((byte) g);
            rgb.add((byte) b);
        }
    }

    private static class ChatNotification {
        public static final int JOINING = 0;
        public static final int LEAVNG = 1;
        public static final int CHAT = 2;
        @SuppressWarnings("unused")
        public int type;
        @SuppressWarnings("unused")
        public UserInfo user;
        @SuppressWarnings("unused")
        public String data;
    }

    private static Set<Session> currentClients = Collections.synchronizedSet(new HashSet<Session>());
    private static Map<Session, UserInfo> usernames = new HashMap<Session, UserInfo>();

    public static void notifySessionsOfMessage(Session exclude, ChatMessage mes) {
        ChatNotification building = new ChatNotification();
        building.type = ChatNotification.CHAT;
        building.user = mes.user;
        building.data = mes.data;
        notifySessionsOfMessage(exclude, building);
    }

    public static void notifySessionsOfJoining(Session joining) {
        ChatNotification building = new ChatNotification();
        building.type = ChatNotification.JOINING;
        if (usernames.containsKey(joining))
            building.user = usernames.get(joining);
        else {
            building.user = new UserInfo("UNKNOWN", defaultIcon, 0, 0, 0, 0);
        }
        notifySessionsOfMessage(null, building);
    }

    public static void notifySessionsOfLeaving(Session leaving) {
        ChatNotification building = new ChatNotification();
        building.type = ChatNotification.LEAVNG;
        if (usernames.containsKey(leaving))
            building.user = usernames.get(leaving);
        else {
            building.user = new UserInfo("UNKNOWN", defaultIcon, 0, 0, 0, 0);
        }
        notifySessionsOfMessage(null, building);
    }

    public static void notifySessionsOfMessage(Session exclude, ChatNotification mes) {
        for (Session session : currentClients) {
            if ((Chat.getChatPersonIsIn(session) != null && session != exclude) || exclude == null) {
                session.getAsyncRemote().sendText(gson.toJson(mes));
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
        System.out.println(
                session.getMaxIdleTimeout());
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
            notifySessionsOfLeaving(session);
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

        try {
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
                    Chat.getPublicChatById(data.roomID).addPerson(data.user, session);
                    usernames.put(session, data.user);
                    session.getAsyncRemote()
                            .sendText("{\"status\": \"Connected to chat.\", \"id\": \"" + session.getId() + "\"}");
                    notifySessionsOfJoining(session);
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
        } catch (Exception e) {
            session.getAsyncRemote().sendText("{\"status\": \"Internal Server Error\", \"error\": \"" + e.getCause() + "\"}");
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
