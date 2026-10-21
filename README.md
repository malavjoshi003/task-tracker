# Task Tracker

Small CSE706 Group 6 project: one server-rendered page, Java 17, Spring Boot,
Thymeleaf and a local H2 database. No login, separate frontend build, or cloud
database is required. Keep this app local; it intentionally has no user accounts.

## Run on Windows

1. Install JDK 17 or later. `java -version` must report a compatible JDK.
2. Extract or clone this project and open a terminal in the folder containing `pom.xml`.
3. Set `JAVA_HOME` to your JDK if Java is not already configured.
4. Run `./mvnw.cmd spring-boot:run` in PowerShell.
5. Open <http://localhost:8080>.

For macOS/Linux: `chmod +x mvnw`, then `./mvnw spring-boot:run`.
The first run downloads Maven and dependencies, so internet access is needed.
Stop with Ctrl+C. Run `./mvnw.cmd test` for unit/integration tests and
`./mvnw.cmd verify` for the normal build. No separate Maven installation is needed.

If port 8080 is busy, run `./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`.
If Windows blocks wrapper downloads through the temporary directory, set `$env:TEMP`
and `$env:TMP` to an existing writable folder before running the wrapper.

## Features

- Create, view and edit tasks; mark complete without deleting them.
- Optional category and due date; filter by category.
- Overdue means incomplete and due before today in Asia/Kolkata.
- Total/pending/completed counts and a basic completion chart use all tasks.
- Saves to `data/tasktracker.mv.db`; stop/start from the same folder to retain data.
- Empty state, blank-title validation, safe repeated completion and cancelled edits.

## Structure

```text
src/main/java/com/group6/tasktracker/
  TaskTrackerApplication.java  application entry point
  TaskItem.java                persistent model
  TaskRepository.java          storage abstraction
  TaskService.java             task and progress rules
  TaskController.java          request handling and form feedback
src/main/resources/
  templates/index.html         one-page interface
  static/style.css             styling
  application.properties       local database settings
src/test/                      JUnit tests
functional-tests/              Selenium browser tests
docs/                          Jira mapping and honest evidence templates
.github/workflows/ci.yml        automated build and tests
```

## Team workflow

Repository: https://github.com/malavjoshi003/task-tracker

Parisha coordinates Scrum and UI; Malav coordinates product/backlog and backend;
Poojaben coordinates testing/CI/metrics; Garima coordinates records and contributes
implementation. Everyone reviews changes and understands the whole app.

Use feature branches and pull requests with another member reviewing before merge.
Do not commit `data/`, `target/`, local secrets or test screenshots with private data.
Acceptance criteria remain in Jira. See `docs/JIRA_GUIDE.md` for task mapping.

## Selenium

See `functional-tests/README.md`. These tests launch an isolated app with an
in-memory database and require Chrome, Python and the Selenium package.

## Course evidence

The supplied code is an implementation aid. The group must run, understand,
review and document it. Do not claim these files prove your team held Scrum
events, paired, performed TDD or passed a GitHub Actions run. Keep genuine
logs, reviewed PRs, chart data and execution results. See `docs/EVIDENCE.md`.

## Push to the existing repository

Preserve the remote history by cloning it first, copying the source into that
clone (including `.github`, `.mvn`, and `.gitignore`), reviewing changes, and
committing on a branch. Do not copy `target`, `data`, or a different `.git` folder.

```powershell
git clone https://github.com/malavjoshi003/task-tracker.git
cd task-tracker
git switch -c codex/task-tracker-mini-project
# Copy the delivered source contents here, including hidden files.
git add .
git commit -m "TTTDA-30 Set up small Task Tracker application"
git push -u origin codex/task-tracker-mini-project
```

Open a pull request on GitHub and ask a teammate to review. The complete feature
implementation goes beyond TTTDA-30: map the changes honestly to the related Jira
items rather than claiming that setup alone implements all sprint work.
