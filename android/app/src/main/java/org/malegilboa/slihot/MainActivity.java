package org.malegilboa.slihot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.DateFormat;
import android.icu.util.HebrewCalendar;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Offline reader for the combined Selichot service. */
public final class MainActivity extends Activity {
  private static final ZoneId JERUSALEM = ZoneId.of("Asia/Jerusalem");
  private static final String START = "התחלה";
  private static final String END = "המשך";
  private static final List<String> CONTENT_FILES = Arrays.asList(
      START, "יום ראשון עמוד 24", "יום שני עמוד 25", "יום שלישי עמוד 26",
      "יום רביעי עמוד 27", "יום חמישי עמוד 28", "יום שישי עמוד 29",
      "יום שביעי עמוד 30", "ערב ראש השנה עמוד 31", "צום גדליה עמוד 34",
      "יום שני של עשרת ימי תשובה עמוד 36", "יום שלישי של עשרת ימי תשובה עמוד 38",
      "יום רביעי של עשרת ימי תשובה עמוד 40", "יום חמישי של עשרת ימי תשובה עמוד 42",
      "ערב יום כיפור עמוד 44", END);

  private TextView currentMiddle;
  private TextView hebrewDate;
  private TextView document;
  private ScrollView scrollView;
  private Instant selectedDate;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(createReader());
    showService();
  }

  private View createReader() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(16), dp(20), dp(16), dp(20));
    root.setBackgroundColor(color("cream"));

    LinearLayout header = new LinearLayout(this);
    header.setOrientation(LinearLayout.VERTICAL);
    header.setGravity(Gravity.RIGHT);
    LinearLayout headerRow = new LinearLayout(this);
    headerRow.setOrientation(LinearLayout.HORIZONTAL);
    headerRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    headerRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    Button menu = button("תוכן");
    menu.setOnClickListener(view -> showContentMenu());
    headerRow.addView(menu, new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    hebrewDate = new TextView(this);
    hebrewDate.setId(R.id.hebrew_date);
    hebrewDate.setTextColor(color("muted_brown"));
    hebrewDate.setTextDirection(View.TEXT_DIRECTION_RTL);
    LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    dateParams.setMarginStart(dp(12));
    headerRow.addView(hebrewDate, dateParams);
    header.addView(headerRow);
    currentMiddle = new TextView(this);
    currentMiddle.setId(R.id.current_middle);
    currentMiddle.setTextColor(color("muted_brown"));
    currentMiddle.setTextDirection(View.TEXT_DIRECTION_RTL);
    header.addView(currentMiddle);
    root.addView(header);

    scrollView = new ScrollView(this);
    scrollView.setFillViewport(true);
    LinearLayout paper = new LinearLayout(this);
    paper.setOrientation(LinearLayout.VERTICAL);
    paper.setPadding(dp(20), dp(24), dp(20), dp(24));
    paper.setBackground(paperBackground());

    document = new TextView(this);
    document.setId(R.id.service_document);
    document.setTextColor(color("ink"));
    document.setTextSize(20);
    document.setLineSpacing(dp(8), 1f);
    document.setTextDirection(View.TEXT_DIRECTION_RTL);
    document.setGravity(Gravity.RIGHT);
    document.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
    document.setMovementMethod(LinkMovementMethod.getInstance());
    document.setLinkTextColor(color("brown"));
    paper.addView(document);

    Button toTop = button("חזרה לראש העמוד");
    LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    topParams.gravity = Gravity.CENTER_HORIZONTAL;
    topParams.topMargin = dp(20);
    paper.addView(toTop, topParams);
    toTop.setOnClickListener(view -> scrollView.smoothScrollTo(0, 0));

    scrollView.addView(paper);
    root.addView(scrollView, new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
    return root;
  }

  private void showService() {
    Instant date = configuredDate();
    hebrewDate.setText(hebrewDateDisplay(date));
    String middle = middleFile(date);
    currentMiddle.setText(middle == null ? "" : withoutPage(middle));
    if (middle == null) {
      document.setText("לא אומרים סליחות היום");
      return;
    }
    showFiles(Arrays.asList(START, middle, END));
  }

  private Instant configuredDate() {
    return selectedDate == null ? Instant.now() : selectedDate;
  }

  private void showContentMenu() {
    String[] choices = new String[CONTENT_FILES.size() + 2];
    choices[0] = "בחירת תאריך לסליחות";
    choices[1] = "חזרה לסדר של היום";
    for (int index = 0; index < CONTENT_FILES.size(); index += 1) choices[index + 2] = CONTENT_FILES.get(index);
    new AlertDialog.Builder(this)
        .setTitle("בחירת קטע")
        .setItems(choices, (dialog, which) -> {
          if (which == 0) showDatePicker();
          else if (which == 1) {
            selectedDate = null;
            showService();
          }
          else {
            currentMiddle.setText("");
            showFiles(Arrays.asList(CONTENT_FILES.get(which - 2)));
          }
        })
        .show();
  }

  private void showDatePicker() {
    LocalDate initial = (selectedDate == null ? Instant.now() : selectedDate).atZone(JERUSALEM).toLocalDate();
    new DatePickerDialog(this, (view, year, month, day) -> {
      selectedDate = LocalDate.of(year, month + 1, day).atTime(12, 0).atZone(JERUSALEM).toInstant();
      showService();
    }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
  }

  private void showFiles(List<String> files) {
    try {
      StringBuilder markdown = new StringBuilder();
      for (String file : files) markdown.append(readAsset(file)).append("\n\n");
      document.setText(renderMarkdown(markdown.toString()));
      scrollView.post(() -> scrollView.scrollTo(0, 0));
    } catch (IOException error) {
      document.setText("לא ניתן לטעון את תוכן הסליחות");
    }
  }

  private String readAsset(String file) throws IOException {
    StringBuilder contents = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        getAssets().open(file + ".md"), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) contents.append(line).append('\n');
    }
    return contents.toString();
  }

  /** A deliberately small Markdown renderer matching the document's source conventions. */
  private Spanned renderMarkdown(String markdown) {
    SpannableStringBuilder output = new SpannableStringBuilder();
    for (String block : markdown.trim().split("\\n\\s*\\n")) {
      String text = block.trim();
      if (text.isEmpty()) continue;
      int start = output.length();
      if (text.equals(">עמוד<")) {
        output.append("────────  עמוד  ────────");
        output.setSpan(new RelativeSizeSpan(.8f), start, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else {
        String[] lines = text.split("\\n");
        boolean verse = block.matches("(?s).*\\n\\s{2,}.*");
        int headingLevel = headingLevel(text);
        for (int index = 0; index < lines.length; index += 1) {
          if (index > 0) output.append('\n');
          String line = lines[index].replaceFirst("^\\s{0,3}(?:[-*+]\\s+|\\d+[.)]\\s+)", "• ");
          line = line.replaceFirst("^#{1,6}\\s+", "");
          appendInline(output, line);
        }
        if (headingLevel > 0) {
          output.setSpan(new StyleSpan(Typeface.BOLD), start, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          output.setSpan(new RelativeSizeSpan(headingLevel == 1 ? 1.45f : 1.18f), start, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (verse) {
          output.setSpan(new StyleSpan(Typeface.ITALIC), start, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      output.append("\n\n");
    }
    return output;
  }

  private void appendInline(SpannableStringBuilder output, String line) {
    int offset = 0;
    while (offset < line.length()) {
      int link = linkStart(line, offset);
      int bold = line.indexOf("**", offset);
      if (link < 0 && bold < 0) { output.append(line.substring(offset)); return; }
      if (link >= 0 && (bold < 0 || link < bold)) {
        output.append(line, offset, link);
        int closeBracket = line.indexOf("](", link + 1);
        int closeParen = line.indexOf(")", closeBracket + 2);
        String label = line.substring(link + 1, closeBracket);
        String target = line.substring(closeBracket + 2, closeParen);
        int start = output.length();
        output.append(label);
        if (isLinkTarget(target)) {
          output.setSpan(new URLSpan(target), start, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        offset = closeParen + 1;
      } else {
        output.append(line, offset, bold);
        int close = line.indexOf("**", bold + 2);
        if (close < 0) { output.append(line.substring(bold)); return; }
        int start = output.length();
        output.append(line, bold + 2, close);
        output.setSpan(new StyleSpan(Typeface.BOLD), start, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        offset = close + 2;
      }
    }
  }

  /** Index of a Markdown link `[label](target)` at or after {@code offset}, or -1. */
  private static int linkStart(String line, int offset) {
    int link = line.indexOf("[", offset);
    while (link >= 0) {
      int closeBracket = line.indexOf("](", link + 1);
      if (closeBracket >= 0) return link;
      link = line.indexOf("[", link + 1);
    }
    return -1;
  }

  private static boolean isLinkTarget(String target) {
    return target.startsWith("http://") || target.startsWith("https://") || target.startsWith("mailto:");
  }

  private static int headingLevel(String text) {
    int index = 0;
    while (index < text.length() && text.charAt(index) == '#') index += 1;
    return index > 0 && index < text.length() && text.charAt(index) == ' ' ? index : 0;
  }

  static String middleFile(Instant moment) {
    LocalDate date = SelichotSchedule.effectiveServiceDate(moment);
    HebrewDate hebrew = hebrewDate(date);
    LocalDate roshHashana = roshHashanaNear(date);
    return roshHashana == null ? null : SelichotSchedule.select(date, hebrew.month, hebrew.day, roshHashana);
  }

  private static LocalDate roshHashanaNear(LocalDate date) {
    for (int offset = -8; offset <= 7; offset += 1) {
      LocalDate candidate = date.plusDays(offset);
      HebrewDate hebrew = hebrewDate(candidate);
      if (hebrew.month == HebrewCalendar.TISHRI && hebrew.day == 1) return candidate;
    }
    return null;
  }

  private static HebrewDate hebrewDate(LocalDate date) {
    HebrewCalendar calendar = new HebrewCalendar();
    calendar.setTimeZone(android.icu.util.TimeZone.getTimeZone("Asia/Jerusalem"));
    calendar.setTime(Date.from(date.atStartOfDay(JERUSALEM).toInstant()));
    return new HebrewDate(calendar.get(HebrewCalendar.DAY_OF_MONTH), calendar.get(HebrewCalendar.MONTH));
  }

  private static String hebrewDateDisplay(Instant moment) {
    DateFormat formatter = DateFormat.getDateInstance(
        DateFormat.LONG, Locale.forLanguageTag("he-IL-u-ca-hebrew"));
    formatter.setTimeZone(android.icu.util.TimeZone.getTimeZone("Asia/Jerusalem"));
    return formatter.format(Date.from(moment));
  }

  private static final class HebrewDate {
    final int day;
    final int month;
    HebrewDate(int day, int month) { this.day = day; this.month = month; }
  }

  private Button button(String label) {
    Button button = new Button(this);
    button.setText(label);
    button.setTextColor(color("brown"));
    button.setAllCaps(false);
    button.setBackground(buttonBackground());
    return button;
  }

  private GradientDrawable paperBackground() {
    GradientDrawable background = new GradientDrawable();
    background.setColor(color("paper"));
    background.setCornerRadius(dp(4));
    background.setStroke(dp(1), Color.rgb(222, 212, 196));
    return background;
  }

  private GradientDrawable buttonBackground() {
    GradientDrawable background = new GradientDrawable();
    background.setColor(color("paper"));
    background.setCornerRadius(dp(5));
    background.setStroke(dp(1), Color.rgb(167, 138, 87));
    return background;
  }

  private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
  private int color(String name) { return getResources().getColor(getResources().getIdentifier(name, "color", getPackageName()), getTheme()); }
  private static String withoutPage(String name) { return name.replaceFirst("\\s*עמוד.*$", "").trim(); }
}
