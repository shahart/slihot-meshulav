package org.malegilboa.slihot;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.Test;

public final class SelichotScheduleTest {
  private static final LocalDate ROSH_HASHANA_2026 = LocalDate.parse("2026-09-12");
  private static final LocalDate ROSH_HASHANA_2024 = LocalDate.parse("2024-10-03");

  @Test public void selectsElulAndAseretYemeiTeshuvaFiles() {
    assertFile("2026-09-05", SelichotSchedule.ELUL, 23, ROSH_HASHANA_2026, "יום ראשון עמוד 24");
    assertFile("2026-09-10", SelichotSchedule.ELUL, 28, ROSH_HASHANA_2026, "יום חמישי עמוד 28");
    assertFile("2026-09-11", SelichotSchedule.ELUL, 29, ROSH_HASHANA_2026, "ערב ראש השנה עמוד 31");
    assertFile("2026-09-15", SelichotSchedule.TISHRI, 4, ROSH_HASHANA_2026, "יום שני של עשרת ימי תשובה עמוד 36");
    assertFile("2026-09-18", SelichotSchedule.TISHRI, 7, ROSH_HASHANA_2026, "יום חמישי של עשרת ימי תשובה עמוד 42");
  }

  @Test public void excludesRoshHashanaAndShabbat() {
    assertFile("2026-09-12", SelichotSchedule.TISHRI, 1, ROSH_HASHANA_2026, null);
    assertFile("2026-09-13", SelichotSchedule.TISHRI, 2, ROSH_HASHANA_2026, null);
    assertFile("2026-09-19", SelichotSchedule.TISHRI, 8, ROSH_HASHANA_2026, null);
  }

  @Test public void handlesPostponedGedaliahSequence() {
    assertFile("2024-10-05", SelichotSchedule.TISHRI, 3, ROSH_HASHANA_2024, null);
    assertFile("2024-10-06", SelichotSchedule.TISHRI, 4, ROSH_HASHANA_2024, "צום גדליה עמוד 34");
    assertFile("2024-10-07", SelichotSchedule.TISHRI, 5, ROSH_HASHANA_2024, "יום שני של עשרת ימי תשובה עמוד 36");
    assertFile("2024-10-11", SelichotSchedule.TISHRI, 9, ROSH_HASHANA_2024, "ערב יום כיפור עמוד 44");
  }

  @Test public void mapsEveryElulDayFromTheShabbatBeforeRoshHashana() {
    assertFile("2026-09-05", SelichotSchedule.ELUL, 23, ROSH_HASHANA_2026, "יום ראשון עמוד 24");
    assertFile("2026-09-06", SelichotSchedule.ELUL, 24, ROSH_HASHANA_2026, "יום ראשון עמוד 24");
    assertFile("2026-09-08", SelichotSchedule.ELUL, 26, ROSH_HASHANA_2026, "יום שלישי עמוד 26");
    assertFile("2026-09-09", SelichotSchedule.ELUL, 27, ROSH_HASHANA_2026, "יום רביעי עמוד 27");
    assertFile("2026-09-10", SelichotSchedule.ELUL, 28, ROSH_HASHANA_2026, "יום חמישי עמוד 28");
  }

  @Test public void returnsNullOutsideTheServiceWindow() {
    assertFile("2026-09-04", SelichotSchedule.ELUL, 22, ROSH_HASHANA_2026, null);
    assertFile("2026-09-21", SelichotSchedule.TISHRI, 10, ROSH_HASHANA_2026, null);
    assertFile("2024-09-27", SelichotSchedule.ELUL, 24, ROSH_HASHANA_2024, null);
    assertFile("2024-10-12", SelichotSchedule.TISHRI, 10, ROSH_HASHANA_2024, null);
  }

  @Test public void handlesTuesdayRoshHashanaWithSkippedShabbat() {
    LocalDate roshHashana = LocalDate.parse("2025-09-23");
    assertFile("2025-09-20", SelichotSchedule.ELUL, 27, roshHashana, "יום ראשון עמוד 24");
    assertFile("2025-09-22", SelichotSchedule.ELUL, 29, roshHashana, "ערב ראש השנה עמוד 31");
    assertFile("2025-09-25", SelichotSchedule.TISHRI, 3, roshHashana, "צום גדליה עמוד 34");
    assertFile("2025-09-26", SelichotSchedule.TISHRI, 4, roshHashana, "יום שני של עשרת ימי תשובה עמוד 36");
    assertFile("2025-09-27", SelichotSchedule.TISHRI, 5, roshHashana, null);
    assertFile("2025-09-28", SelichotSchedule.TISHRI, 6, roshHashana, "יום שלישי של עשרת ימי תשובה עמוד 38");
    assertFile("2025-09-30", SelichotSchedule.TISHRI, 8, roshHashana, "יום חמישי של עשרת ימי תשובה עמוד 42");
    assertFile("2025-10-01", SelichotSchedule.TISHRI, 9, roshHashana, "ערב יום כיפור עמוד 44");
  }

  @Test public void mapsEveryAseretYemeiTeshuvaDayFor2024() {
    assertFile("2024-10-07", SelichotSchedule.TISHRI, 5, ROSH_HASHANA_2024, "יום שני של עשרת ימי תשובה עמוד 36");
    assertFile("2024-10-08", SelichotSchedule.TISHRI, 6, ROSH_HASHANA_2024, "יום שלישי של עשרת ימי תשובה עמוד 38");
    assertFile("2024-10-09", SelichotSchedule.TISHRI, 7, ROSH_HASHANA_2024, "יום רביעי של עשרת ימי תשובה עמוד 40");
    assertFile("2024-10-10", SelichotSchedule.TISHRI, 8, ROSH_HASHANA_2024, "יום חמישי של עשרת ימי תשובה עמוד 42");
  }

  @Test public void switchesToTheNextServiceAtEightPmJerusalem() {
    assertEquals(LocalDate.parse("2026-09-11"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-09-11T16:00:00Z")));
    assertEquals(LocalDate.parse("2026-09-11"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-09-11T16:59:59Z")));
    assertEquals(LocalDate.parse("2026-09-12"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-09-11T17:00:00Z")));
  }

  @Test public void switchesToTheNextServiceAtEightPmStandardTime() {
    assertEquals(LocalDate.parse("2026-01-15"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-01-15T17:59:59Z")));
    assertEquals(LocalDate.parse("2026-01-16"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-01-15T18:00:00Z")));
  }

  private static void assertFile(String date, int month, int day, LocalDate roshHashana, String expected) {
    assertEquals(expected, SelichotSchedule.select(LocalDate.parse(date), month, day, roshHashana));
  }
}
