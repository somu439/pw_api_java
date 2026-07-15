package one.two.three.steps;

import one.two.three.support.ApiContext;
import one.two.three.support.Config;
import one.two.three.utils.CsvUtils;
import one.two.three.utils.JsonPathUtils;
import one.two.three.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSteps {

    private final ApiContext ctx;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApiSteps(ApiContext ctx) {
        this.ctx = ctx;
    }

    @Given("the base URL is configured")
    public void theBaseUrlIsConfigured() {
        ctx.baseUrl = Config.BASE_URL;
    }

    @Given("the base URL is {string}")
    public void theBaseUrlIs(String baseUrl) {
        ctx.baseUrl = baseUrl;
    }

    @Given("the request has JWT token {string}")
    public void theRequestHasJwtToken(String token) {
        ctx.jwtToken = token;
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String endpoint) throws Exception {
        ctx.requestMethod = "GET";
        ctx.requestUrl = ctx.baseUrl + endpoint;
        ctx.response = ctx.getApiContext().get(endpoint);
        ctx.responseBody = ResponseUtils.parseBody(mapper, ctx.response.text());
        attachRequestAndResponse();
    }

    private void attachRequestAndResponse() throws Exception {
        Map<String, Object> requestDetails = new LinkedHashMap<>();
        requestDetails.put("method", ctx.requestMethod);
        requestDetails.put("url", ctx.requestUrl);
        requestDetails.put("headers", Config.HEADERS);
        String requestJson = "REQUEST:\n" + mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(requestDetails);
        ctx.scenario.attach(requestJson.getBytes(), "text/plain", "Request");

        Map<String, Object> responseDetails = new LinkedHashMap<>();
        responseDetails.put("status", ctx.response.status());
        responseDetails.put("statusText", ctx.response.statusText());
        responseDetails.put("headers", ctx.response.headers());
        responseDetails.put("body", ctx.responseBody);
        String responseJson = "RESPONSE:\n" + mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(responseDetails);
        ctx.scenario.attach(responseJson.getBytes(), "text/plain", "Response");
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) {
        assertThat(ctx.response.status())
            .as("Response status code")
            .isEqualTo(statusCode);
    }

    @Then("the response should be OK")
    public void theResponseShouldBeOk() {
        assertThat(ctx.response.ok())
            .as("Response should be OK (2xx)")
            .isTrue();
    }

    @Then("the response header {string} should contain {string}")
    public void theResponseHeaderShouldContain(String header, String value) {
        String headerValue = ctx.response.headers().get(header.toLowerCase());
        assertThat(headerValue)
            .as("Header '%s'", header)
            .isNotNull()
            .contains(value);
    }

    @Then("the response should contain field {string}")
    public void theResponseShouldContainField(String field) {
        JsonPathUtils.read(ctx.responseBody, field);
    }

    @Then("the response field {string} should equal {int}")
    public void theResponseFieldShouldEqual(String field, int expected) {
        Object value = JsonPathUtils.read(ctx.responseBody, field);
        assertThat(((Number) value).intValue())
            .as("Response field '%s'", field)
            .isEqualTo(expected);
    }

    @Then("each of the following fields should be non-null:")
    public void eachOfTheFollowingFieldsShouldBeNonNull(DataTable table) {
        for (String jsonPath : table.asList(String.class)) {
            JsonPathUtils.assertNonNull(ctx.responseBody, jsonPath);
        }
    }

    @Then("the value of the element {string} is {string}")
    public void theValueOfTheElementIs(String elementPath, String expected) {
        JsonPathUtils.assertEquals(ctx.responseBody, elementPath, expected);
    }

    @Then("the value of the element {string} contains {string}")
    public void theValueOfTheElementContains(String elementPath, String expected) {
        JsonPathUtils.assertContains(ctx.responseBody, elementPath, expected);
    }

    @Then("the value of the element {string} contains")
    public void theValueOfTheElementContainsAll(String elementPath, DataTable table) {
        List<String> expectedValues = table.asList(String.class);
        JsonPathUtils.assertContainsAll(ctx.responseBody, elementPath, expectedValues);
    }

    @Then("the following fields should match for {string} and {string}")
    public void theFollowingFieldsShouldMatchFor(String path1, String path2, DataTable table) {
        List<List<String>> expectedPairs = table.asLists(String.class);
        List<String> mismatches = JsonPathUtils.findParallelFieldMismatches(ctx.responseBody, path1, path2, expectedPairs);

        if (!mismatches.isEmpty()) {
            String p1 = JsonPathUtils.normalize(path1);
            String p2 = JsonPathUtils.normalize(path2);
            String warning = "WARNING - Correlated field mismatch(es) between '" + p1 + "' and '" + p2 + "':\n" +
                mismatches.stream()
                    .map(m -> "  - " + m)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");
            ctx.scenario.log(warning);
            ctx.scenario.attach(warning, "text/plain", "correlated-fields-warning");
        }
    }

    @Then("each value at {string} should contain valid date format {string}")
    public void eachValueAtShouldContainValidDateFormat(String jsonPath, String format) {
        JsonPathUtils.assertValidDateFormat(ctx.responseBody, jsonPath, format);
    }

    @Then("each value at {string} should be within the valid list from CSV {string}")
    public void eachValueAtShouldBeWithinValidListFromCsv(String jsonPath, String csvFile) throws Exception {
        List<String> validValues = CsvUtils.readValues(csvFile);
        List<String> mismatches = JsonPathUtils.findMismatches(ctx.responseBody, jsonPath, validValues);

        if (!mismatches.isEmpty()) {
            String path = JsonPathUtils.normalize(jsonPath);
            String warning = "WARNING - '" + path + "' list mismatch(es):\n" +
                mismatches.stream()
                    .map(m -> "  - " + m)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");
            ctx.scenario.log(warning);
            ctx.scenario.attach(warning, "text/plain", csvFile + "-list-warning");
        }
    }
}
