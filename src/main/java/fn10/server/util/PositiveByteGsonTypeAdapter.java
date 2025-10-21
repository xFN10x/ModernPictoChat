package fn10.server.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PositiveByteGsonTypeAdapter implements JsonSerializer<Byte>, JsonDeserializer<Byte> {

    @Override
    public Byte deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        int got = json.getAsInt() - 128;
        return ((byte)got);
    }

    @Override
    public JsonElement serialize(Byte src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src + 128, int.class);
    }

}
