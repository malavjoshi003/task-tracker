# Start the mini project

## Run the delivered ZIP on Windows

1. Extract the entire ZIP to a normal writable folder.
2. Install Java 17 or later if needed. The delivery machine already has JDK 17.
3. Double-click `run-app.cmd`.
4. Wait until the terminal says the application has started.
5. Open http://localhost:8080 in your browser.
6. Keep the terminal open. Ctrl+C stops the application.

The ZIP includes a compiled `task-tracker.jar`, so no Maven downloads are needed
for this route. `data/` will be created beside it. Relaunch from the same extracted
folder to keep saved tasks. The app is intentionally local and single-user.

## Add source to your GitHub repository

The online repository was not updated: local authentication could not complete.

The working source is also saved as a local Git branch:
`codex/task-tracker-mini-project`, based on your repository's original main commit.
From a normal terminal opened in the delivered source folder, you can run:

```powershell
git push -u origin codex/task-tracker-mini-project
```

Sign in through Git's normal GitHub login if prompted. Then open GitHub and create
a pull request to main for your teammate to review. The downloaded ZIP omits `.git`,
so if using only the ZIP, clone the remote first as explained in README and copy
the source into that clone.

For browser upload, use GitHub Add file > Upload files and upload extracted source
folders/files, including `.github` and `.mvn`. Do not upload the ZIP itself, the
compiled `task-tracker.jar`, `data/`, or `target/`. A normal Git clone/push is more
reliable for preserving dot folders and executable settings.

## Evidence for TTTDA-30

- Repository or PR link after publishing.
- Screenshot of the application running locally.
- README with setup and test instructions.
- A teammate's actual confirmation that they could clone and run it.

Only mark the Jira setup task Done after your agreed acceptance criteria pass.
The source includes later features too; map and review them against their own
stories rather than automatically marking all stories complete.

See README for development, tests, limitations and team workflow.
