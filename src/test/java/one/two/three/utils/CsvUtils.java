package one.two.three.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvUtils {

    private CsvUtils() {
    }

    public static List<String> readValues(String csvFile) throws Exception {
        InputStream is = CsvUtils.class.getClassLoader()
            .getResourceAsStream("csv/" + csvFile + ".csv");

        assertThat(is)
            .as("CSV file 'csv/%s.csv' should exist on classpath", csvFile)
            .isNotNull();

        List<String> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    values.add(line);
                }
            }
        }
        return values;
    }
}
