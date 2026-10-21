# Local verification — 21 October 2026

The delivered application was checked locally on Windows with Java 17.

| Check | Actual result |
| --- | --- |
| JUnit unit and integration tests | 12 passed, zero failures/errors/skips |
| Executable JAR packaging | Passed |
| Selenium with Chrome | One complete browser workflow passed |
| File database persistence | Created a task, stopped the application, restarted it, and confirmed the task remained |

The browser workflow covers adding, editing, cancelling an edit, completing,
category filtering, overdue indication, progress counts, and blank-title validation.
The database uses synchronous writes (`WRITE_DELAY=0`) for this small prototype.

The restricted local environment required an explicit Byte Buddy Java agent for
test instrumentation and Chrome's sandbox disabled for the isolated browser test.
Normal development and CI instructions use the standard Maven commands; the
browser test only disables its sandbox when TEST_NO_SANDBOX=1 is explicitly set.

An initial packaging attempt encountered a Windows file lock because the app was
running. After stopping the app, packaging succeeded. These are local results,
not evidence of a GitHub Actions run or a team review.

GitHub publishing could not complete because local GitHub authentication was
unavailable. The team must publish, review, run CI, and record its own Scrum and
TDD evidence. Do not use these checks as proof of a red/green TDD history.
