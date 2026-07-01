# pw_api — Playwright Java + Cucumber API Tests

## Project Structure

```
src/test/
├── java/com/example/
│   ├── runner/TestRunner.java       # Cucumber TestNG runner
│   ├── support/
│   │   ├── Config.java              # Loads env properties, headers, JWT token
│   │   └── ApiContext.java          # Shared state across steps (like World in TS)
│   ├── hooks/Hooks.java             # Before/After scenario hooks
│   └── steps/ApiSteps.java          # Step definitions
└── resources/
    ├── features/get-product.feature # Cucumber feature file
    ├── env/
    │   ├── dev.properties           # Dev environment config
    │   ├── stage.properties         # Stage environment config
    │   ├── stage1.properties        # Stage1 environment config
    │   └── stage2.properties        # Stage2 environment config
    └── csv/ratings.csv              # Valid rating enum values
testng.xml                           # TestNG suite descriptor
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
