import assert from "node:assert/strict";
import test from "node:test";
import { dateFromDebugFlag, middleFile } from "../docs/app.mjs";

// Noon UTC is safely within the intended Jerusalem civil day for these dates.
const gregorianDate = date => new Date(`${date}T12:00:00Z`);

test("middleFile selects content from an injected Gregorian date", () => {
  const cases = [
    ["2026-09-04", null, "the day before the Shabbat before Rosh Hashanah"],
    ["2026-09-05", "יום ראשון עמוד 24", "the Shabbat before Rosh Hashanah"],
    ["2026-09-06", "יום ראשון עמוד 24", "the day after the Shabbat before Rosh Hashanah"],
    ["2026-09-07", "יום שני עמוד 25", "the second day after the Shabbat before Rosh Hashanah"],
    ["2026-09-08", "יום שלישי עמוד 26", "the third day after the Shabbat before Rosh Hashanah"],
    ["2026-09-09", "יום רביעי עמוד 27", "the fourth day after the Shabbat before Rosh Hashanah"],
    ["2026-09-10", "יום חמישי עמוד 28", "the fifth day after the Shabbat before Rosh Hashanah"],
    ["2026-09-11", "ערב ראש השנה עמוד 31", "29 Elul"],
    ["2026-09-12", null, "the first day of Rosh Hashanah"],
    ["2026-09-13", null, "the second day of Rosh Hashanah"],
    ["2026-09-14", "צום גדליה עמוד 34", "3 Tishri"],
    ["2026-09-15", "יום שני של עשרת ימי תשובה עמוד 36", "4 Tishri"],
    ["2026-09-16", "יום שלישי של עשרת ימי תשובה עמוד 38", "5 Tishri"],
    ["2026-09-17", "יום רביעי של עשרת ימי תשובה עמוד 40", "6 Tishri"],
    ["2026-09-18", "יום חמישי של עשרת ימי תשובה עמוד 42", "7 Tishri"],
    ["2026-09-19", null, "Shabbat in the middle of the Tishri sequence"],
    ["2026-09-20", "ערב יום כיפור עמוד 44", "9 Tishri"],
    ["2026-09-21", null, "after 9 Tishri"],
    ["2024-09-27", null, "before the Shabbat before a Thursday Rosh Hashanah"],
    ["2024-09-28", "יום ראשון עמוד 24", "the Shabbat before a Thursday Rosh Hashanah"],
    ["2024-09-29", "יום ראשון עמוד 24", "the first day after the Shabbat before a Thursday Rosh Hashanah"],
    ["2024-09-30", "יום שני עמוד 25", "the second day after the Shabbat before a Thursday Rosh Hashanah"],
    ["2024-10-01", "יום שלישי עמוד 26", "the third day after the Shabbat before a Thursday Rosh Hashanah"],
    ["2024-10-02", "ערב ראש השנה עמוד 31", "29 Elul before a Thursday Rosh Hashanah"],
    ["2024-10-05", null, "3 Tishri falls on Shabbat"],
    ["2024-10-06", "צום גדליה עמוד 34", "the Sunday after a Shabbat 3 Tishri"],
    ["2024-10-07", "יום שני של עשרת ימי תשובה עמוד 36", "first day after postponed 3 Tishri"],
    ["2024-10-08", "יום שלישי של עשרת ימי תשובה עמוד 38", "second day after postponed 3 Tishri"],
    ["2024-10-09", "יום רביעי של עשרת ימי תשובה עמוד 40", "third day after postponed 3 Tishri"],
    ["2024-10-10", "יום חמישי של עשרת ימי תשובה עמוד 42", "fourth day after postponed 3 Tishri"],
    ["2024-10-11", "ערב יום כיפור עמוד 44", "9 Tishri after an extra weekday"],
    ["2025-09-19", null, "before the Shabbat before a Tuesday Rosh Hashanah"],
    ["2025-09-20", "יום ראשון עמוד 24", "the Shabbat before a Tuesday Rosh Hashanah"],
    ["2025-09-25", "צום גדליה עמוד 34", "3 Tishri in 2025"],
    ["2025-09-26", "יום שני של עשרת ימי תשובה עמוד 36", "4 Tishri before a skipped Shabbat"],
    ["2025-09-27", null, "Shabbat during the 2025 Tishri sequence"],
    ["2025-09-28", "יום שלישי של עשרת ימי תשובה עמוד 38", "first weekday after the skipped Shabbat"],
    ["2025-09-29", "יום רביעי של עשרת ימי תשובה עמוד 40", "next weekday after the skipped Shabbat"],
    ["2025-09-30", "יום חמישי של עשרת ימי תשובה עמוד 42", "last intermediate weekday after the skipped Shabbat"],
    ["2025-10-01", "ערב יום כיפור עמוד 44", "9 Tishri after the skipped Shabbat"]
  ];

  for (const [date, expectedFile, description] of cases) {
    assert.equal(middleFile(gregorianDate(date)), expectedFile, description);
  }
});

test("middleFile treats 20:00-23:59 Jerusalem as the next day", () => {
  const cases = [
    ["2026-09-11T16:00:00Z", "ערב ראש השנה עמוד 31", "19:00 stays on 29 Elul"],
    ["2026-09-11T17:00:00Z", null, "20:00 rolls 29 Elul into the first day of Rosh Hashanah"],
    ["2026-09-11T20:00:00Z", null, "23:00 also rolls into the first day of Rosh Hashanah"],
    ["2026-09-19T16:00:00Z", null, "19:00 on Shabbat stays on Shabbat"],
    ["2026-09-19T17:00:00Z", "ערב יום כיפור עמוד 44", "20:00 on Shabbat advances into 9 Tishri"],
    ["2026-09-13T17:00:00Z", "צום גדליה עמוד 34", "20:00 on the second day of Rosh Hashanah advances to 3 Tishri"]
  ];

  for (const [iso, expectedFile, description] of cases) {
    assert.equal(middleFile(new Date(iso)), expectedFile, description);
  }
});

test("dateFromDebugFlag accepts URL query parameter values", () => {
  const params = new URLSearchParams("SLIHOT_DEBUG_DATE=2026-09-05");

  assert.equal(
    dateFromDebugFlag(params.get("SLIHOT_DEBUG_DATE")).toISOString(),
    "2026-09-05T00:00:00.000Z"
  );
  assert.equal(dateFromDebugFlag(new URLSearchParams().get("SLIHOT_DEBUG_DATE")), null);
  assert.equal(dateFromDebugFlag("not-a-date"), null);
});
