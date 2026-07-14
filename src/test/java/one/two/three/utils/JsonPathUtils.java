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

    public static void assertContains(Object json, String rawPath, String expected) {
        String path = normalize(rawPath);
        Object result = read(json, rawPath);

        if (result instanceof List<?> values) {
            boolean found = values.stream().anyMatch(v -> String.valueOf(v).equals(expected));
            assertThat(found)
                .as("JsonPath '%s' should contain '%s' (actual: %s)", path, expected, values)
                .isTrue();
        } else {
            assertThat(String.valueOf(result))
                .as("Value at '%s'", rawPath)
                .contains(expected);
        }
    }

    // Like assertContains, but checks that every value in expectedValues is present
    // (in any order) rather than just one — for asserting a whole set of array elements at once.
    public static void assertContainsAll(Object json, String rawPath, List<String> expectedValues) {
        String path = normalize(rawPath);
        Object result = read(json, rawPath);
        List<?> values = result instanceof List<?> list ? list : List.of(result);

        List<String> missing = new ArrayList<>();
        for (String expected : expectedValues) {
            boolean found = values.stream().anyMatch(v -> String.valueOf(v).equals(expected));
            if (!found) {
                missing.add(expected);
            }
        }

        if (!missing.isEmpty()) {
            throw new AssertionError(
                "Missing expected value(s) at '" + path + "' (actual: " + values + "):\n"
                    + missing.stream().map(m -> "  - '" + m + "'").reduce((a, b) -> a + "\n" + b).orElse("")
            );
        }
    }

    // Correlates two parallel array projections from the same underlying array (e.g.
    // "reviews[*].comment" and "reviews[*].reviewerName" — JsonPath wildcard projections
    // preserve array order, so index i in one list corresponds to index i in the other).
    // For each (value1, value2) row, finds the index where path1's value equals value1
    // and checks path2's value at that same index equals value2. Generic across any two
    // fields/paths and any number of expected pairs. Returns the mismatch descriptions
    // (empty if all pairs matched) rather than throwing, so callers can choose to warn
    // instead of failing the scenario.
    public static List<String> findParallelFieldMismatches(Object json, String path1, String path2,
                                                            List<List<String>> expectedPairs) {
        String p1 = normalize(path1);
        String p2 = normalize(path2);

        Object result1 = read(json, path1);
        Object result2 = read(json, path2);
        List<?> values1 = result1 instanceof List<?> list ? list : List.of(result1);
        List<?> values2 = result2 instanceof List<?> list ? list : List.of(result2);

        assertThat(values1.size())
            .as("'%s' and '%s' should project the same number of elements", p1, p2)
            .isEqualTo(values2.size());

        List<String> mismatches = new ArrayList<>();
        for (List<String> pair : expectedPairs) {
            String expected1 = pair.get(0);
            String expected2 = pair.get(1);

            int index = -1;
            for (int i = 0; i < values1.size(); i++) {
                if (String.valueOf(values1.get(i)).equals(expected1)) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                mismatches.add(String.format(
                    "no element found where %s = '%s'", p1, expected1
                ));
                continue;
            }

            String actual2 = String.valueOf(values2.get(index));
            if (!actual2.equals(expected2)) {
                mismatches.add(String.format(
                    "at index %d — %s = '%s', expected %s = '%s' but was '%s'",
                    index, indexedLocation(p1, index), expected1, indexedLocation(p2, index), expected2, actual2
                ));
            }
        }

        return mismatches;
    }

    // Resolves the "[*]" wildcard in a path to the concrete index that produced a mismatch,
    // e.g. "$.reviews[*].comment" + 2 -> "$.reviews[2].comment", so the error points at one element.
    private static String indexedLocation(String normalizedPath, int index) {
        return normalizedPath.replace("[*]", "[" + index + "]");
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
