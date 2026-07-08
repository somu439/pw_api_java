package one.two.three.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = {"src/test/resources/features", "target/generated-features"},
    glue = {"one.two.three.steps", "one.two.three.hooks"},
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
