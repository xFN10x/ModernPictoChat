package fn10.server.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fn10.server.chat.Chat;

public class ChatGsonTypeAdapter implements JsonSerializer<Chat>, JsonDeserializer<Chat> {
    private static class Serilized {
        @SuppressWarnings("unused")
        public String name;
        public int id;
    }

    @Override
    public Chat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
                int id = ((Serilized)context.deserialize(json, Serilized.class)).id;
        return Chat.getChat(id);
    }

    @Override
    public JsonElement serialize(Chat src, Type typeOfSrc, JsonSerializationContext context) {
        Serilized building = new Serilized();
        building.id = src.getId();
        building.name = src.Name;
        return context.serialize(building);
    }

}
