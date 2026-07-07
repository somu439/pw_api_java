# pw_api — Playwright Java + Cucumber API Tests

## Project Structure

```
src/test/
├── java/one/two/three/
│   ├── runner/TestRunner.java        # Cucumber TestNG runner
│   ├── support/
│   │   ├── Config.java               # Loads env properties, headers, JWT token
│   │   └── ApiContext.java           # Shared state across steps (like World in TS); lazily builds the Playwright APIRequestContext
│   ├── hooks/Hooks.java              # Before/After scenario hooks
│   ├── steps/ApiSteps.java           # Step definitions
│   └── utils/
│       ├── JsonPathUtils.java        # Reusable JsonPath read/assert helpers (non-null, equality, enum/list checks)
│       └── CsvUtils.java             # Reusable CSV-to-List<String> reader
└── resources/
    ├── features/get-product.feature  # Cucumber feature file
    ├── env/
    │   ├── dev.properties            # Dev environment config
    │   ├── stage.properties          # Stage environment config
    │   ├── stage1.properties         # Stage1 environment config
    │   └── stage2.properties         # Stage2 environment config
    └── csv/
        ├── ratings.csv               # Valid "rating" enum values
        └── comment.csv               # Valid "comment" enum values
testng.xml                            # TestNG suite descriptor
```

## Environment Configuration

Each environment has its own properties file under `src/test/resources/env/`.  
Update `BASE_URL` and `JWT_TOKEN` in each file to match the target environment.

| File | Environment |
|------|-------------|
| `env/dev.properties` | Development |
| `env/stage.properties` | Stage |
| `env/stage1.properties` | Stage 1 |
| `env/stage2.properties` | Stage 2 |

**Resolution order:** environment variable → properties file → default value

The feature file's `Given the base URL is "..."` / `Given the request has JWT token "..."` steps
can also override these per-scenario — they take effect even though they run in the `Background`,
because `ApiContext` builds the underlying Playwright request context lazily on the first HTTP call.

## Running Tests

### Maven (direct)

```bash
# defaults — dev environment, @system and @regression tags
mvn clean verify

# specify environment
mvn clean verify -Denv=stage1
mvn clean verify -Denv=stage2

# specify environment and tags
mvn clean verify -Denv=dev    -Dcucumber.filter.tags="@regression"
mvn clean verify -Denv=stage1 -Dcucumber.filter.tags="@system and @regression"
mvn clean verify -Denv=stage2 -Dcucumber.filter.tags="@system or @regression"
mvn clean verify -Denv=dev    -Dcucumber.filter.tags="not @regression"
```

### Shell script (Linux / Mac)

```bash
# Usage: ./run-tests.sh [env] [tags]

./run-tests.sh                              # dev + default tags
./run-tests.sh stage1                       # stage1 + default tags
./run-tests.sh stage2 "@regression"
./run-tests.sh dev "@system or @regression"
```

### PowerShell (Windows)

```powershell
# Usage: .\run-tests.ps1 [-Env <env>] [-Tags <tags>]

.\run-tests.ps1                                          # dev + default tags
.\run-tests.ps1 -Env stage1                              # stage1 + default tags
.\run-tests.ps1 -Env stage2 -Tags "@regression"
.\run-tests.ps1 -Env dev    -Tags "@system or @regression"
```

The scripts run `mvn clean verify` and open the HTML report automatically when done.

## Tag Expressions

Tags are defined on the Feature or Scenario in `.feature` files (`@system`, `@regression`, etc.)  
and filtered at runtime via `-Dcucumber.filter.tags`.

| Expression | Runs |
|---|---|
| `@regression` | scenarios tagged `@regression` |
| `@system and @regression` | scenarios that have **both** tags |
| `@system or @regression` | scenarios that have **either** tag |
| `not @regression` | scenarios **without** `@regression` |
| `@system and not @regression` | has `@system` but not `@regression` |

The default tag filter (`@system and @regression`) is set in `TestRunner.java` and applies  
when `-Dcucumber.filter.tags` is not passed.

## Reports

| Report | Path |
|--------|------|
| Masterthought HTML | `target/cucumber-html-reports/overview-features.html` |
| Basic HTML | `target/cucumber-reports/index.html` |
| JSON | `target/cucumber-reports/cucumber.json` |

```bash
# open report manually
xdg-open target/cucumber-html-reports/overview-features.html
```

## JWT Token

**Via environment variable:**
```bash
JWT_TOKEN="eyJhbGci..." ./run-tests.sh stage1
```

**Via properties file** — set `JWT_TOKEN=` in the relevant `env/*.properties` file.

**Via feature file step:**
```gherkin
Given the request has JWT token "eyJhbGci..."
```

## Writing Feature Files

All step definitions live in `ApiSteps.java`. The JSON-path-based steps accept any valid
[JsonPath](https://github.com/json-path/JsonPath) expression evaluated against the parsed
response body — write it either bare (`reviews[*].rating`) or fully qualified (`$.reviews[*].rating`);
a leading `body.` (or plain `body`) is also accepted and stripped, since the response body is
what's being addressed. Values are read via `JsonPathUtils` / `CsvUtils` (`utils/` package),
so any new step needing JSON assertions or CSV-backed lookups should reuse those helpers
instead of duplicating parsing logic.

### Available steps

| Step | Purpose |
|---|---|
| `Given the base URL is configured` | Uses `Config.BASE_URL` (env/property default) |
| `Given the base URL is {string}` | Overrides the base URL for this scenario |
| `Given the request has JWT token {string}` | Sets the `Authorization: Bearer` header |
| `When I send a GET request to {string}` | Performs the GET and parses the JSON body |
| `Then the response status should be {int}` | Asserts HTTP status code |
| `Then the response should be OK` | Asserts status is 2xx |
| `Then the response header {string} should contain {string}` | Asserts a response header contains a substring |
| `Then the response should contain field {string}` | Asserts a top-level key exists in the body |
| `Then the response field {string} should equal {int}` | Asserts a top-level integer field's value |
| `Then each of the following fields should be non-null:` (DataTable) | Asserts each JsonPath resolves to a non-null value (or all elements, if it matches an array) |
| `Then the value of the element {string} is {string}` | Asserts a JsonPath resolves to an exact expected value |
| `Then each value at {string} should be within the valid list from CSV {string}` | Checks each matched value against a CSV allow-list; mismatches are logged/attached as a **warning**, not a failure |

### Example

```gherkin
@system @regression
Feature: GET Product - DummyJSON API
  As a QA engineer
  I want to verify the GET product endpoint of dummyjson.com
  So that I can ensure product data is returned correctly

  Background:
    Given the base URL is "https://dummyjson.com"

  Scenario: Get a single product by ID
    When I send a GET request to "/products/1"
    Then the response status should be 200
    And the response should be OK
    And the response header "content-type" should contain "application/json"
    And the response should contain field "id"
    And the response field "id" should equal 1
    And the response should contain field "title"

    # Non-null checks — plain field ("title") or array element field ("reviews[*].reviewerEmail")
    And each of the following fields should be non-null:
      | reviews[*].reviewerEmail |
      | reviews[*].reviewerName  |
      | title                    |

    # Enum/allow-list checks against csv/ratings.csv and csv/comment.csv (header row names the field,
    # mismatches are reported as warnings in the report rather than failing the scenario)
    And each value at "reviews[*].rating" should be within the valid list from CSV "ratings"
    And each value at "reviews[*].comment" should be within the valid list from CSV "comment"

    # Exact-value checks, including nested objects and array indices
    And the value of the element "body.dimensions.depth" is "22.99"
    And the value of the element "body.tags[1]" is "mascara"
    And the value of the element "body.sku" is "BEA-ESS-ESS-001"
```

Add new `csv/<name>.csv` files (header row + one valid value per line) to back additional
`should be within the valid list from CSV "<name>"` checks without touching any Java code.
