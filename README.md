# AuxbyAndroidApp

The repository for Android mobile app.

Ensure that the following fields are set in the `local.properties` file for signing the release build:
`KEY_ALIAS`, `KEY_PASSWORD`, `STORE_FILE`, and `STORE_PASSWORD`.

Do not commit sensitive information like passwords to version control.

**Run unittests with coverage**
- Debug: ./gradlew testDebugUnitTestCoverage 
- Release: ./gradlew testReleaseUnitTestCoverage

**Jacoco** report: /app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html