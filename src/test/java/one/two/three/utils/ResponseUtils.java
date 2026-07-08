package one.two.three.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ResponseUtils {

    private ResponseUtils() {
    }

    // Accepts either a plain JSON object (the usual API response) or a single-element array
    // wrapping a {status, statusText, headers, body} envelope, and returns the actual body map either way.
    public static Map<String, Object> parseBody(ObjectMapper mapper, String rawJson) throws Exception {
        JsonNode node = mapper.readTree(rawJson);

        if (node.isArray()) {
            node = node.isEmpty() ? mapper.createObjectNode() : node.get(0);
        }

        if (node.has("body") && (node.has("status") || node.has("statusText"))) {
            node = node.get("body");
        }

        return mapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
    }
}
