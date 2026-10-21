# Jira mapping

TTTDA-30 setup: Maven wrapper, application entry point, folder structure,
README and runnable app. Evidence: repository/PR URL, startup screenshot and
another member's successful clone/run check. Do not mark it Done until that check.

| Story | Behavior | Implementation |
|---|---|---|
| TTTDA-38 | Add task | TaskService.create and create form |
| TTTDA-39 | View tasks | TaskService.list and task table |
| TTTDA-40 | Edit details | edit form and TaskService.update |
| TTTDA-41 | Complete | TaskService.complete |
| TTTDA-42 | Persistence | TaskRepository and file-backed H2 |
| TTTDA-43 | Categories | category validation/form |
| TTTDA-44 | Due dates | optional LocalDate field |
| TTTDA-45 | Overdue | TaskService.overdue and date badge |
| TTTDA-46 | Filter | category filter; global counts unchanged |
| TTTDA-47 | Progress | progress summary and chart |

The standalone sprint feature items TTTDA-31 through 35 overlap with those
stories. Consolidate the work and estimates with the team; do not award points
twice. TTTDA-36 covers TDD evidence and TTTDA-37 covers CI evidence.

Keep setup, implementation and real Scrum evidence distinct. The group needs two
sprint planning records, daily notes, reviews, retrospectives, a documented code
review or pairing session per sprint, charts, risks and testing reports.

Suggested demo: empty page, reject blank title, add task, edit, cancel another
edit, complete, add category and deadline, filter, inspect global chart, restart
and verify persistence. Review with the actual acceptance criteria before Done.
