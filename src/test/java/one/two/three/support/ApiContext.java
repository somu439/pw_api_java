package one.two.three.support;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.Scenario;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public Scenario scenario;

    // Created lazily so Given steps (which run after @Before) can still change baseUrl/jwtToken first.
    public APIRequestContext getApiContext() {
        if (apiContext == null) {
            Map<String, String> headers = new LinkedHashMap<>(Config.HEADERS);
            if (jwtToken != null && !jwtToken.isEmpty()) {
                headers.put("Authorization", "Bearer " + jwtToken);
            }
            apiContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                    .setBaseURL(baseUrl)
                    .setExtraHTTPHeaders(headers)
            );
        }
        return apiContext;
    }
}
