package fn10.server.chat;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.NameNotFoundException;

import fn10.server.endpoints.ChatEndpoint;
import fn10.server.endpoints.ChatEndpoint.UserInfo;
import jakarta.websocket.Session;

public class Chat {

    private static List<Chat> chats = new ArrayList<Chat>();
    static {
        chats.add(new Chat("Chat A", 0));
        chats.add(new Chat("Chat B", 1));
        chats.add(new Chat("Chat C", 2));
        chats.add(new Chat("Chat D", 3));
        chats.add(new Chat("Chat E",4));
        chats.add(new Chat("Chat F", 5));
        chats.add(new Chat("Chat G", 6));
    }

    /**
     * 
     * @param id the id corresponding to the chat
     * @return the chat found, or null if it isnt found
     */
    @Deprecated
    public static Chat getPublicChatById(int id) {
        for (Chat chats : chats) {
            if (chats.id == id)
                return chats;
        }
        return null;
    }

    public static Chat getChat(String name) {
        for (Chat chat : chats) {
            if (chat.Name.equals(name))
                return chat;
        }
        return null;
    }

    public static Chat getChat(int id) {
        for (Chat chat : chats) {
            if (chat.id == id)
                return chat;
        }
        return null;
    }

    public static List<Chat> getChats() {
        return chats;
    }
    

    public static Chat getChatPersonIsIn(Session ses) {
        for (Chat chat : chats) {
            if (chat.peopleInHere.containsKey(ses))
                return chat;
        }
        return null;
    }

    private static class ChatInfo {
        @SuppressWarnings("unused")
        public String name;
        @SuppressWarnings("unused")
        public int id;
        @SuppressWarnings("unused")
        public int maxPeople;
        @SuppressWarnings("unused")
        public int peopleCurrently;

        public ChatInfo(String name, int id, int max, int rn) {
            this.name = name;
            this.maxPeople = max;
            this.peopleCurrently = rn;
            this.id = id;
        }
    }

    /**
     * 
     * @return a Map, with the key being the chat id, and the value being the amount
     *         of people in it
     */
    public static Map<Integer, ChatInfo> getChatIdsAndPeopleInThem() {
        LinkedHashMap<Integer, ChatInfo> building = new LinkedHashMap<Integer, ChatInfo>();
        for (Chat chat : chats) {
            building.put(chat.id, new ChatInfo(chat.Name, chat.getId(), chat.maxPeople, chat.peopleInHere.size()));
        }
        return building;
    }

    public static class ChatMessage {
        public UserInfo user;
        public Instant dateSent;
        public String data;
        public Chat chatSent;
    }

    private final int maxPeople = 15;
    private final int id;
    public final String Name;
    private Map<Session, UserInfo> peopleInHere = new HashMap<Session, UserInfo>();
    private List<ChatMessage> messages = new ArrayList<ChatMessage>();

    public int getId() {
        return id;
    }

    public boolean userIsInChat(String username) {
        for (UserInfo value : peopleInHere.values()) {
            if (value.username.equals(username))
                return true;
        }
        return false;
    }

    public void sendMessage(String senderID, String data) {
        Session sender = null;
        for (Session ses : peopleInHere.keySet()) {
            if (ses.getId().equals(senderID)) {
                sender = ses;
                break;
            }
        }
        if (sender == null) {
            System.out.println("(" + getClass().getSimpleName() + ")  Session " + senderID
                    + ", isnt in here! Chat: " + Name);
        }

        if (peopleInHere.containsKey(sender)) {
            ChatMessage message = new ChatMessage();
            message.user = peopleInHere.get(sender);
            message.dateSent = Instant.now();
            message.data = data;
            message.chatSent = this;
            messages.add(message);
            System.out.println("Chat sent by " + senderID);
            try {
                ChatEndpoint.SendTextToSession(sender, "{\"status\": \"Sent Chat\", \"chatStatus\": \"200\"}");
            } catch (NameNotFoundException | IOException e) {
                e.printStackTrace();
                System.out.println("Session couldnt get status, removing chat");
                try {
                    ChatEndpoint.SendTextToSession(sender, "{\"status\": \"Failed\", \"chatStatus\": \"401\"}");
                } catch (NameNotFoundException | IOException e1) {
                }
                messages.remove(message);
                return;
            }
            ChatEndpoint.notifySessionsOfMessage(sender, message);

        } else {
            System.out.println("(" + getClass().getSimpleName() + ")  Session " + sender.getId()
                    + ", isnt in here! Chat: " + Name);
        }
    }

    public Chat(String name, int id) {
        this.Name = name;
        this.id = id;
    }

    public int getAmountOfPeopleInChat() {
        return peopleInHere.size();
    }

    public Map<Session, UserInfo> getPeopleInChat() {
        return peopleInHere;
    }

    public int getPeopleMax() {
        return maxPeople;
    }

    /**
     * 
     * @param user The info of the user joining. Required: Username & Colour
     * @param ses The session connected to that user
     * @return a bool indicating if the user was added
     */
    public boolean addPerson(UserInfo user, Session ses) {
        Chat in = getChatPersonIsIn(ses);
        if (in != null && in != this) {
            ses.getAsyncRemote()
                    .sendText("{\"status\": \"Tried to join chat already in.\", \"shownError\": \"Cannot be in multiple chats at once.\"}");
            return false;
        } else if (in == this) {
            ses.getAsyncRemote()
                    .sendText("{\"status\": \"User is already in this chat.\", \"shownMessage\": \"Already in this chat.\"}");
            return false;
        }
        peopleInHere.put(Objects.requireNonNull(ses), Objects.requireNonNull(user));
        ses.getAsyncRemote()
                .sendText("{\"status\": \"Connected to chat.\", \"id\": \"" + ses.getId() + "\"}");
        return true;
    }

    public void removePerson(Session ses) {
        peopleInHere.remove(ses);
    }

}
