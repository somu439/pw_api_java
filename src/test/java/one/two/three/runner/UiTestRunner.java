package one.two.three.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = {"src/test/resources/features-ui"},
    glue = {"one.two.three.ui.steps", "one.two.three.ui.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/ui-index.html",
        "json:target/cucumber-reports/ui-cucumber.json"
    },
    monochrome = true,
    tags = "@ui"
)
public class UiTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
