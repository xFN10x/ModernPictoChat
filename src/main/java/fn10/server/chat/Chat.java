package fn10.server.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.session.Session;

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

    public static List<Chat> getChats() {
        return chats;
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

    private final int maxPeople = 15;
    private final int id;
    public final String Name;
    private Map<String, Session> peopleInHere = new HashMap<String, Session>();

    public Chat(String name, int id) {
        this.Name = name;
        this.id = id;
    }

    public int getPeopleInChat() {
        return peopleInHere.size();
    }

    public int getPeopleMax() {
        return maxPeople;
    }

    public void addPerson(String username, Session ses) {
        peopleInHere.put(username, ses);
    }

    public void removePerson(String username) {
        peopleInHere.remove(username);
    }

}
