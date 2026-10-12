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

  @Test public void switchesToTheNextServiceAtEightPmJerusalem() {
    assertEquals(LocalDate.parse("2026-09-11"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-09-11T16:00:00Z")));
    assertEquals(LocalDate.parse("2026-09-12"), SelichotSchedule.effectiveServiceDate(Instant.parse("2026-09-11T17:00:00Z")));
  }

  private static void assertFile(String date, int month, int day, LocalDate roshHashana, String expected) {
    assertEquals(expected, SelichotSchedule.select(LocalDate.parse(date), month, day, roshHashana));
  }
}
