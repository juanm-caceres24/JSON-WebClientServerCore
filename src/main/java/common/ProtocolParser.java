package common;

import com.google.gson.Gson; 

/**
 * Utility class responsible for converting JsonPacket objects to raw JSON strings
 * and vice versa. It ensures standardized serialization across the network.
 */
public class ProtocolParser {
    private static final Gson gson = new Gson();

    /**
     * Serializes a JsonPacket object into a single-line JSON string.
     */
    public static String serialize(JsonPacket packet) {
        return gson.toJson(packet);
    }

    /**
     * Deserializes a raw JSON string back into a JsonPacket object.
     */
    public static JsonPacket deserialize(String json) {
        return gson.fromJson(json, JsonPacket.class);
    }
}
