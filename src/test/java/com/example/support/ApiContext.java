package com.example.support;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;

import java.util.HashMap;
import java.util.Map;

public class ApiContext {

    public Playwright playwright;
    public APIRequestContext apiContext;
    public APIResponse response;
    public Map<String, Object> responseBody = new HashMap<>();
    public String baseUrl = Config.BASE_URL;
    public String jwtToken = Config.JWT_TOKEN;
    public String requestUrl = "";
    public String requestMethod = "";

    public Object getField(String field) {
        return responseBody.get(field);
    }
}
