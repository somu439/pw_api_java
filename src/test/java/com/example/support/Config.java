package com.example.support;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final String BASE_URL =
        System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "https://dummyjson.com";

    public static final String JWT_TOKEN =
        System.getenv("JWT_TOKEN") != null ? System.getenv("JWT_TOKEN") : "";

    public static final Map<String, String> HEADERS = buildHeaders();

    private static Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        if (!JWT_TOKEN.isEmpty()) {
            headers.put("Authorization", "Bearer " + JWT_TOKEN);
        }
        return headers;
    }
}
