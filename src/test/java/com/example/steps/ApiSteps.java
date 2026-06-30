package com.example.steps;

import com.example.support.ApiContext;
import com.example.support.Config;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        ctx.response = ctx.apiContext.get(endpoint);
        ctx.responseBody = mapper.readValue(
            ctx.response.text(),
            new TypeReference<Map<String, Object>>() {}
        );
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
        assertThat(ctx.responseBody)
            .as("Response body should contain field '%s'", field)
            .containsKey(field);
    }

    @Then("the response field {string} should equal {int}")
    public void theResponseFieldShouldEqual(String field, int expected) {
        Object value = ctx.getField(field);
        assertThat(((Number) value).intValue())
            .as("Response field '%s'", field)
            .isEqualTo(expected);
    }

    @Then("each review should have non-null field {string}")
    public void eachReviewShouldHaveNonNullField(String field) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reviews = (List<Map<String, Object>>) ctx.responseBody.get("reviews");

        assertThat(reviews)
            .as("reviews array")
            .isNotNull()
            .isInstanceOf(List.class);

        for (int i = 0; i < reviews.size(); i++) {
            Object value = reviews.get(i).get(field);
            assertThat(value)
                .as("reviews[%d].%s must not be null or undefined", i, field)
                .isNotNull();
        }
    }

    @Then("each review rating should be within the valid enum from {string}")
    public void eachReviewRatingShouldBeWithinValidEnum(String csvFile) throws Exception {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream("csv/" + csvFile + ".csv");

        assertThat(is)
            .as("CSV file 'csv/%s.csv' should exist on classpath", csvFile)
            .isNotNull();

        List<Integer> validValues = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (firstLine) { firstLine = false; continue; } // skip header
                if (!line.isEmpty()) {
                    validValues.add(Integer.parseInt(line));
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reviews = (List<Map<String, Object>>) ctx.responseBody.get("reviews");

        assertThat(reviews)
            .as("reviews array")
            .isNotNull()
            .isInstanceOf(List.class);

        List<String> mismatches = new ArrayList<>();
        for (int i = 0; i < reviews.size(); i++) {
            int rating = ((Number) reviews.get(i).get("rating")).intValue();
            if (!validValues.contains(rating)) {
                mismatches.add(String.format(
                    "reviews[%d].rating = %d (not in enum: [%s])",
                    i, rating, validValues.stream()
                        .map(String::valueOf)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")
                ));
            }
        }

        if (!mismatches.isEmpty()) {
            throw new AssertionError("Rating enum mismatch(es):\n" +
                mismatches.stream()
                    .map(m -> "  - " + m)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse(""));
        }
    }
}
