# Repository Guidelines

## Project Structure & Module Organization

This is a dependency-free static site for the combined Selichot service. Keep the published entry point in `docs/index.html`; its styles and markup live there. Browser behavior, the Hebrew-calendar selection rules, Markdown rendering, and the content picker live in `docs/app.mjs`.

Source material belongs in `docs/`. The site loads `התחלה`, one date-selected middle file, and `end`; individual files use names such as `יום ראשון עמוד 24`, `ערב ראש השנה עמוד 31`, and `ערב יום כיפור עמוד 44`. Put Node unit tests in `test/`, alongside `middle-file.test.mjs`.

## Build, Test, and Development Commands

There is no build step or package manager. Serve the repository root with any static web server so browser `fetch()` can load the Markdown files:

```sh
python3 -m http.server
# Open http://localhost:8000/docs/
```

Run the full test suite with:

```sh
node --test test/*.test.mjs
```

Check module syntax without starting a server:

```sh
node --check docs/app.mjs
```

## Coding Style & Naming Conventions

Use two-space indentation in HTML, CSS, and JavaScript. Keep the site dependency-free and use standard browser APIs. Use `camelCase` for JavaScript functions and variables, `SCREAMING_SNAKE_CASE` only for globals such as `window.SLIHOT_DEBUG_DATE`, and lowercase underscore-separated Markdown filenames.

Preserve the `Asia/Jerusalem` calendar context. `middleFile(today)` accepts an injected Gregorian `Date`; retain that seam instead of hard-coding dates. Do not use `innerHTML` with unescaped Markdown content—pass user/content text through `escapeHtml`.

## Testing Guidelines

Use Node's built-in `node:test` and `node:assert/strict`; no third-party test framework is configured. Add a table-driven case to `test/middle-file.test.mjs` for every calendar rule or boundary changed. Create dates at noon UTC (for example, `new Date("2026-09-05T12:00:00Z")`) so assertions remain inside the intended Jerusalem civil day.

## Commit & Pull Request Guidelines

Use short imperative subjects, for example `Handle Rosh Hashanah exclusion`. In a pull request, explain the calendar/content behavior changed, list test commands run, and include a screenshot for visible changes to `docs/index.html`.
