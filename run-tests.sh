#!/bin/bash

MVN="/home/sreeni/.m2/wrapper/dists/apache-maven-3.8.4-bin/52ccbt68d252mdldqsfsn03jlf/apache-maven-3.8.4/bin/mvn"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPORT="$PROJECT_DIR/target/cucumber-html-reports/overview-features.html"

ENV="${1:-dev}"

cd "$PROJECT_DIR"

echo "================================================"
echo " Running Cucumber Tests  [env=$ENV]"
echo "================================================"

$MVN clean verify -Denv="$ENV"

EXIT_CODE=$?

echo ""
echo "================================================"

if [ -f "$REPORT" ]; then
    echo " Report generated: $REPORT"
    echo " Opening report in browser..."
    xdg-open "$REPORT"
else
    echo " Report not found at: $REPORT"
fi

echo "================================================"
exit $EXIT_CODE
