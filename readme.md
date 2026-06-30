# pw_api — Playwright Java + Cucumber API Tests

## Project Structure

```
src/test/
├── java/com/example/
│   ├── runner/TestRunner.java       # Cucumber JUnit runner
│   ├── support/
│   │   ├── Config.java              # Base URL, headers, JWT token
│   │   └── ApiContext.java          # Shared state across steps (like World in TS)
│   ├── hooks/Hooks.java             # Before/After scenario hooks
│   └── steps/ApiSteps.java          # Step definitions
└── resources/
    ├── features/get-product.feature # Cucumber feature file
    └── csv/ratings.csv              # Valid rating enum values
```

## Run Tests & Generate Report

### Option 1 — Shell script (recommended)
```bash
./run-tests.sh
```
Runs `mvn clean verify` and opens the Cucumber HTML report automatically.

### Option 2 — Maven command
```bash
/home/sreeni/.m2/wrapper/dists/apache-maven-3.8.4-bin/52ccbt68d252mdldqsfsn03jlf/apache-maven-3.8.4/bin/mvn clean verify
```

### Open report manually
```bash
xdg-open target/cucumber-html-reports/overview-features.html
```

## Reports

| Report | Path |
|--------|------|
| Masterthought HTML | `target/cucumber-html-reports/overview-features.html` |
| Basic HTML | `target/cucumber-reports/index.html` |
| JSON | `target/cucumber-reports/cucumber.json` |

## JWT Token

**Via environment variable:**
```bash
JWT_TOKEN="eyJhbGci..." ./run-tests.sh
```

**Via feature file step:**
```gherkin
Given the request has JWT token "eyJhbGci..."
```

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `BASE_URL` | `https://dummyjson.com` | API base URL |
| `JWT_TOKEN` | _(none)_ | Bearer token for Authorization header |
