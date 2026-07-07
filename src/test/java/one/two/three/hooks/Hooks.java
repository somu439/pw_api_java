package one.two.three.hooks;

import one.two.three.support.ApiContext;
import one.two.three.support.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void setUp(Scenario scenario) {
        ctx.scenario = scenario;
        ctx.playwright = Playwright.create();
        ctx.apiContext = null;
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

        if (ctx.apiContext != null) {
            ctx.apiContext.dispose();
        }
        ctx.playwright.close();
    }
}
