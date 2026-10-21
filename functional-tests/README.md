# Browser acceptance tests

Prerequisites: JDK 17+, Python 3.10+, Google Chrome, internet on first driver setup.

From the repository root:

```powershell
./mvnw.cmd package
python -m pip install -r functional-tests/requirements.txt
python functional-tests/test_workflow.py
```

The script starts its own app on an unused local port with an isolated in-memory
database, launches headless Chrome, and stops both afterward. It does not touch
your normal saved tasks. Selenium Manager obtains a suitable browser driver.
Optional environment variables: `JAVA_EXE` (Java executable), `CHROME_BINARY`,
`CHROMEDRIVER` (preinstalled driver), and `TEST_SCREENSHOT` (PNG path).

It checks empty state, creation, cancelled edit, saved edit, completion, global
progress under filtering, empty filter results, overdue status and blank-title
rejection. JUnit covers business rules and repository/request integration.
Keep the actual command output as report evidence; do not claim an unrun test passed.
