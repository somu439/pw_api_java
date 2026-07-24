# pw_api — Playwright Java + Cucumber UI Tests

Browser-driven tests against ParaBank, using the same Maven/Cucumber/TestNG stack as the
API suite (see [readme.md](readme.md)) but with its own runner, glue package, and feature
directory so the two suites stay independent.

## Project Structure

```
src/test/
├── java/one/two/three/
│   ├── runner/UiTestRunner.java       # Cucumber TestNG runner (tags = "@ui")
│   ├── ui/
│   │   ├── support/UiContext.java     # Shared Playwright state across UI steps (page, browser, context...)
│   │   ├── hooks/UiHooks.java         # @Before/@After("@ui") — browser lifecycle, screenshot/video/trace capture
│   │   ├── steps/LoginSteps.java      # Step definitions (currently holds locators inline — see "Adding a New Page" below)
│   │   └── pages/                     # (recommended, create as needed) Page Objects for new pages
│   └── support/Config.java            # PARABANK_BASE_URL, PARABANK_USERNAME/PASSWORD, HEADLESS
└── resources/
    └── features-ui/
        ├── login.feature              # @ui @regression
        └── dashboard.feature          # @ui @regression
```

Generated at runtime (gitignored under `target/`):

```
target/
├── pw-videos/     # .webm recordings, one per scenario — kept only if the scenario failed
├── pw-traces/     # trace.zip files, one per scenario — kept only if the scenario failed
└── cucumber-reports/, cucumber-html-reports/   # shared with the API suite
```

## Running UI Tests

The `UiTestRunner` annotation defaults to `tags = "@ui"`, but `-Dcucumber.filter.tags`
(wired through in `pom.xml`) overrides it, so pass `@ui` explicitly to run just this suite.

```bash
# Maven direct
mvn clean verify -Dcucumber.filter.tags="@ui"
mvn clean verify -Dcucumber.filter.tags="@ui" -Denv=stage1

# Shell script (Linux/Mac) — opens the report automatically when done
./run-tests.sh dev "@ui"

# PowerShell (Windows)
.\run-tests.ps1 -Env dev -Tags "@ui"
```

Browser visibility is controlled by `HEADLESS` (env var → `env/*.properties` → default
`false`), resolved in `Config.java`:

```bash
HEADLESS=true mvn clean verify -Dcucumber.filter.tags="@ui"
```

ParaBank credentials for the "valid credentials" steps come from `PARABANK_USERNAME` /
`PARABANK_PASSWORD`, resolved the same way.

## Screenshots, Videos & Traces

`UiHooks` (`@Before`/`@After("@ui")`) wraps every `@ui` scenario:

| Artifact | Captured | Kept when | Attached to Cucumber report |
|---|---|---|---|
| Screenshot (full page, PNG) | On failure only | — | Yes, rendered inline |
| Video (`.webm`, 1280×720) | Every scenario | Only if the scenario **failed** | Yes, as a downloadable attachment |
| Trace (`.zip`, screenshots + DOM snapshots + sources) | Every scenario | Only if the scenario **failed** | Yes, as a downloadable attachment |

Passing scenarios have their video/trace files deleted in the `@After` hook to avoid
disk clutter — only failures leave artifacts behind in `target/pw-videos` /
`target/pw-traces`, and those same bytes are what get attached to the Cucumber JSON
(so they also show up in the aggregated report).

**View the aggregated report** (screenshots inline, video/trace as attachments):

```bash
xdg-open target/cucumber-html-reports/overview-features.html
```

**View a trace file directly:**

```bash
npx playwright show-trace target/pw-traces/<scenario-name>-<timestamp>-trace.zip
```

(or drag-and-drop the zip onto https://trace.playwright.dev). This opens the timeline,
DOM snapshots, network, and console for that scenario.

**View a video file directly:** open the `.webm` in any browser or media player that
supports WebM (e.g. `xdg-open target/pw-videos/<file>.webm`).

## UI Feature Files (`features-ui/`)

| File | Tags | Covers |
|---|---|---|
| `login.feature` | `@ui @regression` | Successful login, invalid-credentials error message |
| `dashboard.feature` | `@ui @regression` | Accounts Overview dashboard content after login |

All scenarios must carry `@ui` (or a tag combination that still matches the filter you
run with) since that's what routes them to `UiTestRunner`'s glue (`one.two.three.ui.steps`,
`one.two.three.ui.hooks`) instead of the API runner's. Tag filtering works the same way
as the API suite — see the "Tag Expressions" section in [readme.md](readme.md).

## Adding a New Page and Its Locators

Locators currently live inline in `LoginSteps.java`. For any **new** page, use a Page
Object instead of putting locators directly in a steps class — it keeps one page's
selectors in one place and steps files reusable/readable.

1. **Create the Page Object** under `ui/pages/`, taking `com.microsoft.playwright.Page`
   in its constructor and exposing behavior methods (not raw locators) to steps:

   ```java
   package one.two.three.ui.pages;

   import com.microsoft.playwright.Locator;
   import com.microsoft.playwright.Page;

   public class TransferFundsPage {
       private final Page page;

       public TransferFundsPage(Page page) {
           this.page = page;
       }

       private Locator amountInput()      { return page.locator("#amount"); }
       private Locator fromAccountSelect(){ return page.locator("#fromAccountId"); }
       private Locator toAccountSelect()  { return page.locator("#toAccountId"); }
       private Locator transferButton()   { return page.locator("input[value='Transfer']"); }

       public void transfer(String amount, String fromAccount, String toAccount) {
           amountInput().fill(amount);
           fromAccountSelect().selectOption(fromAccount);
           toAccountSelect().selectOption(toAccount);
           transferButton().click();
       }

       public String confirmationText() {
           return page.locator("#showResult").innerText();
       }
   }
   ```

   Locators are exposed as private methods (re-queried each call) rather than cached
   fields, so they stay valid across navigations within the same scenario.

2. **Create the steps class** under `ui/steps/`, injected with `UiContext` (PicoContainer
   wires the same instance `UiHooks` populated for this scenario) and delegating to the
   Page Object — no `ctx.page.locator(...)` calls directly in the steps file:

   ```java
   package one.two.three.ui.steps;

   import io.cucumber.java.en.Then;
   import io.cucumber.java.en.When;
   import one.two.three.ui.pages.TransferFundsPage;
   import one.two.three.ui.support.UiContext;

   import static org.assertj.core.api.Assertions.assertThat;

   public class TransferFundsSteps {
       private final UiContext ctx;
       private TransferFundsPage transferFundsPage;

       public TransferFundsSteps(UiContext ctx) {
           this.ctx = ctx;
       }

       private TransferFundsPage page() {
           if (transferFundsPage == null) {
               transferFundsPage = new TransferFundsPage(ctx.page);
           }
           return transferFundsPage;
       }

       @When("I transfer {string} from account {string} to account {string}")
       public void iTransfer(String amount, String from, String to) {
           page().transfer(amount, from, to);
       }

       @Then("I should see a transfer confirmation")
       public void iShouldSeeConfirmation() {
           assertThat(page().confirmationText()).contains("has been transferred");
       }
   }
   ```

   The Page Object is instantiated lazily (first use) because `ctx.page` doesn't exist
   until `UiHooks.setUp` has run for the scenario — building it in the steps class
   constructor would capture a stale/`null` page.

3. **Add the feature file** under `features-ui/`, tagged `@ui` (+ any other tags you
   filter on):

   ```gherkin
   @ui @regression
   Feature: ParaBank Transfer Funds

     Background:
       Given I am on the ParaBank login page
       And I log in with valid credentials

     Scenario: Transfer funds between own accounts
       When I transfer "100" from account "12345" to account "67890"
       Then I should see a transfer confirmation
   ```

4. No changes to `UiTestRunner` or glue config are needed — `glue = {"one.two.three.ui.steps", "one.two.three.ui.hooks"}`
   already picks up any new class under those packages, and `ui/pages/` classes are
   plain POJOs instantiated by the steps classes, not glue themselves.

5. Run it:

   ```bash
   mvn clean verify -Dcucumber.filter.tags="@ui"
   ```
