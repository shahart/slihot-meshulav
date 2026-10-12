package org.malegilboa.slihot;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import android.view.View;
import android.widget.DatePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class MainActivityTest {
  @Test public void showsReaderControlsAndHebrewDate() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      onView(withText("תוכן")).check(matches(isDisplayed()));
      onView(withText("חזרה לראש העמוד")).check(matches(isDisplayed()));
      onView(withId(R.id.service_document)).check(matches(isDisplayed()));
      onView(withId(R.id.hebrew_date)).check(matches(not(withText(""))));
    }
  }

  @Test public void contentMenuLoadsTheChosenFile() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      onView(withText("תוכן")).perform(click());
      onView(withText("בחירת קטע")).check(matches(isDisplayed()));
      onView(withText("בחירת תאריך לסליחות")).check(matches(isDisplayed()));

      onView(withText("צום גדליה עמוד 34")).perform(click());

      onView(withId(R.id.service_document)).check(matches(withText(containsString("צום גדליה"))));
      onView(withId(R.id.service_document)).check(matches(withText(containsString("אָבְלָה נַפְשִׁי"))));
      onView(withId(R.id.current_middle)).check(matches(withText("")));
    }
  }

  @Test public void datePickerLoadsTheSelectedServiceDate() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      onView(withText("תוכן")).perform(click());
      onView(withText("בחירת תאריך לסליחות")).perform(click());

      onView(isAssignableFrom(DatePicker.class)).perform(setDate(2026, 8, 14));
      onView(withId(android.R.id.button1)).perform(click());

      onView(withId(R.id.current_middle)).check(matches(withText("צום גדליה")));
      onView(withId(R.id.service_document)).check(matches(withText(containsString("צום גדליה"))));
    }
  }

  @Test public void returningToTodaysOrderClearsTheChosenDate() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      onView(withText("תוכן")).perform(click());
      onView(withText("בחירת תאריך לסליחות")).perform(click());
      onView(isAssignableFrom(DatePicker.class)).perform(setDate(2026, 8, 14));
      onView(withId(android.R.id.button1)).perform(click());
      onView(withId(R.id.current_middle)).check(matches(withText("צום גדליה")));

      onView(withText("תוכן")).perform(click());
      onView(withText("חזרה לסדר של היום")).perform(click());

      onView(withId(R.id.current_middle)).check(matches(not(withText("צום גדליה"))));
    }
  }

  private static ViewAction setDate(final int year, final int month, final int day) {
    return new ViewAction() {
      @Override public Matcher<View> getConstraints() {
        return isAssignableFrom(DatePicker.class);
      }

      @Override public String getDescription() {
        return "set the date picker to " + year + "-" + (month + 1) + "-" + day;
      }

      @Override public void perform(UiController uiController, View view) {
        ((DatePicker) view).updateDate(year, month, day);
      }
    };
  }
}
