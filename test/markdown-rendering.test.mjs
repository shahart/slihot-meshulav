import assert from "node:assert/strict";
import test from "node:test";
import { renderMarkdown } from "../docs/app.mjs";

test("renderMarkdown renders Markdown headings", () => {
  assert.equal(
    renderMarkdown("### כמנהג הספרדים"),
    "<h3>כמנהג הספרדים</h3>"
  );
});
