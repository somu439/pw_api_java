#!/bin/bash
# Usage: ./run-tests.sh [env] [tags]
#   env   : dev | stage | stage1 | stage2  (default: dev)
#   tags  : cucumber tag expression        (default: @system and @regression)
#
# Examples:
#   ./run-tests.sh
#   ./run-tests.sh stage1
#   ./run-tests.sh stage2 "@regression"
#   ./run-tests.sh dev "@system or @regression"

MVN="/home/sreeni/.m2/wrapper/dists/apache-maven-3.8.4-bin/52ccbt68d252mdldqsfsn03jlf/apache-maven-3.8.4/bin/mvn"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPORT="$PROJECT_DIR/target/cucumber-html-reports/overview-features.html"

ENV="${1:-dev}"
TAGS="${2:-@system and @regression}"

cd "$PROJECT_DIR"

echo "================================================"
echo " Running Cucumber Tests"
echo " env  : $ENV"
echo " tags : $TAGS"
echo "================================================"

$MVN clean verify -Denv="$ENV" -Dcucumber.filter.tags="$TAGS"

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
