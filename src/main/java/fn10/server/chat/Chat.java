package fn10.server.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.session.Session;

public class Chat {

    private static List<Chat> chats = new ArrayList<Chat>();
    static {
        chats.add(new Chat(0));
        chats.add(new Chat(1));
        /*chats.add(new Chat(2));
        chats.add(new Chat(3));
        chats.add(new Chat(4));
        chats.add(new Chat(5));
        chats.add(new Chat(6));
        chats.add(new Chat(7));*/
    }

    /**
     * 
     * @param id the id corresponding to the chat
     * @return the chat found, or null if it isnt found
     */
    public static Chat getPublicChatById(int id) {
        for (Chat chats : chats) {
            if (chats.id == id) return chats;
        }
        return null;
    }

    public static List<Chat> getChats() {
        return chats;
    }

    private final int maxPeople = 15;
    private final int id;
    private Map<String, Session> peopleInHere = new HashMap<String, Session>();

    public Chat(int id) {
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
