package com.example.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.example.steps", "com.example.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/index.html",
        "json:target/cucumber-reports/cucumber.json"
    },
    monochrome = true,
    tags = "@system and @regression"
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
