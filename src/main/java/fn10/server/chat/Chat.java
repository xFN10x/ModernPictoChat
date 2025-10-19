package fn10.server.chat;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;

import fn10.server.endpoints.ChatEndpoint;
import jakarta.websocket.Session;

public class Chat {

    private static List<Chat> chats = new ArrayList<Chat>();
    static {
        chats.add(new Chat("Chat A", 0));
        chats.add(new Chat("Chat B", 1));
    }

    /**
     * 
     * @param id the id corresponding to the chat
     * @return the chat found, or null if it isnt found
     */
    public static Chat getPublicChatById(int id) {
        for (Chat chats : chats) {
            if (chats.id == id)
                return chats;
        }
        return null;
    }

    public static Chat getChatByName(String name) {
        for (Chat chat : chats) {
            if (chat.Name.equals(name))
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
        public String name;
        public int maxPeople;
        public int peopleCurrently;

        public ChatInfo(String name, int max, int rn) {
            this.name = name;
            this.maxPeople = max;
            this.peopleCurrently = rn;
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
            building.put(chat.id, new ChatInfo(chat.Name, chat.maxPeople, chat.peopleInHere.size()));
        }
        return building;
    }

    public static class ChatMessage {
        public String username;
        public Instant dateSent;
        public String data;

    }

    private final int maxPeople = 15;
    private final int id;
    public final String Name;
    private Map<Session, String> peopleInHere = new HashMap<Session, String>();
    private List<ChatMessage> messages = new ArrayList<ChatMessage>();

    public int getId() {
        return id;
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
            System.out.println("(" + getClass().getSimpleName() + ")  Session " + sender.getId()
                    + ", isnt in here! Chat: " + Name);
        }

        if (peopleInHere.containsKey(sender)) {
            ChatMessage message = new ChatMessage();
            message.username = peopleInHere.get(sender);
            message.dateSent = Instant.now();
            message.data = data;
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

    public Map<Session, String> getPeopleInChat() {
        return peopleInHere;
    }

    public int getPeopleMax() {
        return maxPeople;
    }

    public void addPerson(String username, Session ses) {
        peopleInHere.put(ses, username);
    }

    public void removePerson(Session ses) {
        peopleInHere.remove(ses);
    }

}
