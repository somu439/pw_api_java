package one.two.three.utils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPathUtils {

    private JsonPathUtils() {
    }

    // Strips an optional leading "body."/"body" (callers write paths relative to the response body)
    // and ensures a "$." prefix so the expression is a valid JsonPath.
    public static String normalize(String rawPath) {
        String path = rawPath.equals("body") ? ""
            : rawPath.startsWith("body.") ? rawPath.substring("body.".length())
            : rawPath;
        return path.startsWith("$") ? path : "$." + path;
    }

    public static Object read(Object json, String rawPath) {
        String path = normalize(rawPath);
        try {
            return JsonPath.read(json, path);
        } catch (PathNotFoundException e) {
            throw new AssertionError("JsonPath '" + path + "' was not found in the response", e);
        }
    }

    public static void assertNonNull(Object json, String rawPath) {
        String path = normalize(rawPath);
        Object result = read(json, rawPath);

        if (result instanceof List<?> values) {
            assertThat(values)
                .as("JsonPath '%s' should match at least one element", path)
                .isNotEmpty();
            for (int i = 0; i < values.size(); i++) {
                assertThat(values.get(i))
                    .as("JsonPath '%s'[%d] must not be null", path, i)
                    .isNotNull();
            }
        } else {
            assertThat(result)
                .as("JsonPath '%s' must not be null", path)
                .isNotNull();
        }
    }

    public static void assertEquals(Object json, String rawPath, String expected) {
        Object result = read(json, rawPath);
        assertThat(String.valueOf(result))
            .as("Value at '%s'", rawPath)
            .isEqualTo(expected);
    }

    public static List<String> findMismatches(Object json, String rawPath, List<String> validValues) {
        String path = normalize(rawPath);
        Object result = read(json, rawPath);
        List<?> values = result instanceof List<?> list ? list : List.of(result);

        List<String> mismatches = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            String actual = String.valueOf(values.get(i));
            if (!validValues.contains(actual)) {
                mismatches.add(String.format(
                    "%s[%d] = %s (not in list: [%s])",
                    path, i, actual, String.join(", ", validValues)
                ));
            }
        }
        return mismatches;
    }
}
