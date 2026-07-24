package one.two.three.ui.steps;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import one.two.three.support.Config;
import one.two.three.ui.support.UiContext;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final UiContext ctx;

    public LoginSteps(UiContext ctx) {
        this.ctx = ctx;
    }

    @Given("I am on the ParaBank login page")
    public void iAmOnTheParaBankLoginPage() {
        ctx.page.navigate(ctx.baseUrl + "/index.htm");
    }

    @When("I log in with valid credentials")
    public void iLogInWithValidCredentials() {
        login(Config.PARABANK_USERNAME, Config.PARABANK_PASSWORD);
    }

    @When("I log in with username {string} and password {string}")
    public void iLogInWithUsernameAndPassword(String username, String password) {
        login(username, password);
    }

    private void login(String username, String password) {
        ctx.page.locator("input[name='username']").fill(username);
        ctx.page.locator("input[name='password']").fill(password);
        ctx.page.locator("form[name='login'] input[type='submit']").click();
    }

    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        assertThat(ctx.page.title())
            .as("Page title after login")
            .isEqualTo("ParaBank | Accounts Overview");
        assertThat(ctx.page.locator("#showOverview h1.title").innerText().trim())
            .as("Overview heading")
            .isEqualTo("Accounts Overview");
    }

    @Then("the page title should be {string}")
    public void thePageTitleShouldBe(String expectedTitle) {
        assertThat(ctx.page.title())
            .as("Page title")
            .isEqualTo(expectedTitle);
    }

    @Then("I should see a login error message")
    public void iShouldSeeALoginErrorMessage() {
        Locator error = ctx.page.locator("p.error");
        assertThat(error.innerText())
            .as("Login error message")
            .contains("The username and password could not be verified.");
    }

    @Then("the dashboard should display a welcome message for {string}")
    public void theDashboardShouldDisplayAWelcomeMessageFor(String fullName) {
        Locator welcome = ctx.page.locator("#leftPanel p.smallText");
        assertThat(welcome.innerText())
            .as("Welcome message")
            .contains("Welcome")
            .contains(fullName);
    }

    @Then("the {string} table should be visible")
    public void theTableShouldBeVisible(String tableCaption) {
        Locator heading = ctx.page.locator("#showOverview h1.title");
        assertThat(heading.innerText().trim())
            .as("Table caption heading")
            .isEqualTo(tableCaption);
        assertThat(ctx.page.locator("#accountTable").isVisible())
            .as("'%s' table visible", tableCaption)
            .isTrue();
    }

    @Then("the {string} navigation link should be visible")
    public void theNavigationLinkShouldBeVisible(String linkText) {
        Locator link = ctx.page.locator("#leftPanel").getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(linkText));
        assertThat(link.isVisible())
            .as("Navigation link '%s' visible", linkText)
            .isTrue();
    }
}
