package com.example.hooks;

import com.example.support.ApiContext;
import com.example.support.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.util.LinkedHashMap;
import java.util.Map;

public class Hooks {

    private final ApiContext ctx;
    private final ObjectMapper mapper = new ObjectMapper();

    public Hooks(ApiContext ctx) {
        this.ctx = ctx;
    }

    @Before
    public void setUp() {
        Map<String, String> headers = new LinkedHashMap<>(Config.HEADERS);
        if (ctx.jwtToken != null && !ctx.jwtToken.isEmpty()) {
            headers.put("Authorization", "Bearer " + ctx.jwtToken);
        }
        ctx.playwright = Playwright.create();
        ctx.apiContext = ctx.playwright.request().newContext(
            new APIRequest.NewContextOptions()
                .setBaseURL(ctx.baseUrl)
                .setExtraHTTPHeaders(headers)
        );
        ctx.responseBody = new LinkedHashMap<>();
        ctx.requestUrl = "";
        ctx.requestMethod = "";
    }

    @After
    public void tearDown(Scenario scenario) throws Exception {
        if (!ctx.requestUrl.isEmpty()) {
            Map<String, Object> requestDetails = new LinkedHashMap<>();
            requestDetails.put("method", ctx.requestMethod);
            requestDetails.put("url", ctx.requestUrl);
            requestDetails.put("headers", Config.HEADERS);
            String requestJson = "REQUEST:\n" + mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestDetails);
            scenario.attach(requestJson.getBytes(), "text/plain", "Request");
        }

        if (ctx.response != null) {
            Map<String, Object> responseDetails = new LinkedHashMap<>();
            responseDetails.put("status", ctx.response.status());
            responseDetails.put("statusText", ctx.response.statusText());
            responseDetails.put("headers", ctx.response.headers());
            responseDetails.put("body", ctx.responseBody);
            String responseJson = "RESPONSE:\n" + mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseDetails);
            scenario.attach(responseJson.getBytes(), "text/plain", "Response");
        }

        ctx.apiContext.dispose();
        ctx.playwright.close();
    }
}
