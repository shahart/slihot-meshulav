package org.malegilboa.slihot;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Pure calendar rules, kept free of Android APIs so they can run as JVM tests. */
public final class SelichotSchedule {
  public static final int TISHRI = 0;
  public static final int ELUL = 12;
  private static final ZoneId JERUSALEM = ZoneId.of("Asia/Jerusalem");

  private SelichotSchedule() { }

  public static LocalDate effectiveServiceDate(Instant moment) {
    LocalDate date = moment.atZone(JERUSALEM).toLocalDate();
    return moment.atZone(JERUSALEM).getHour() >= 20 ? date.plusDays(1) : date;
  }

  public static String select(LocalDate date, int hebrewMonth, int hebrewDay, LocalDate roshHashana) {
    LocalDate start = shabbatBefore(roshHashana);
    LocalDate end = roshHashana.plusDays(8);
    if (date.isBefore(start) || date.isAfter(end) ||
        (hebrewMonth == TISHRI && (hebrewDay == 1 || hebrewDay == 2))) return null;
    if (hebrewMonth == ELUL && hebrewDay == 29) return "ערב ראש השנה עמוד 31";
    if (hebrewMonth == ELUL) {
      int days = (int) (date.toEpochDay() - start.toEpochDay());
      String[] files = {"יום ראשון עמוד 24", "יום ראשון עמוד 24", "יום שני עמוד 25", "יום שלישי עמוד 26", "יום רביעי עמוד 27", "יום חמישי עמוד 28", "יום שישי עמוד 29", "יום שביעי עמוד 30"};
      return days >= 0 && days < files.length ? files[days] : null;
    }
    if (hebrewMonth == TISHRI && hebrewDay == 3) return date.getDayOfWeek() == DayOfWeek.SATURDAY ? null : "צום גדליה עמוד 34";
    if (hebrewMonth == TISHRI && hebrewDay == 9) return "ערב יום כיפור עמוד 44";
    if (hebrewMonth == TISHRI && hebrewDay >= 4 && hebrewDay <= 8) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY) return null;
      boolean postponed = date.plusDays(3 - hebrewDay).getDayOfWeek() == DayOfWeek.SATURDAY;
      if (postponed && hebrewDay == 4) return "צום גדליה עמוד 34";
      int firstDay = postponed ? 5 : 4;
      int serviceDay = 0;
      for (int day = firstDay; day <= hebrewDay; day += 1) {
        if (date.plusDays(day - hebrewDay).getDayOfWeek() != DayOfWeek.SATURDAY) serviceDay += 1;
      }
      String[] files = {"יום שני של עשרת ימי תשובה עמוד 36", "יום שלישי של עשרת ימי תשובה עמוד 38", "יום רביעי של עשרת ימי תשובה עמוד 40", "יום חמישי של עשרת ימי תשובה עמוד 42"};
      return serviceDay > 0 && serviceDay <= files.length ? files[serviceDay - 1] : null;
    }
    return "יום ראשון עמוד 24";
  }

  private static LocalDate shabbatBefore(LocalDate roshHashana) {
    for (int days = 1; days <= 7; days += 1) {
      LocalDate candidate = roshHashana.minusDays(days);
      if (candidate.getDayOfWeek() == DayOfWeek.SATURDAY) return candidate;
    }
    throw new IllegalStateException("Cannot find the Shabbat before Rosh Hashanah");
  }
}
