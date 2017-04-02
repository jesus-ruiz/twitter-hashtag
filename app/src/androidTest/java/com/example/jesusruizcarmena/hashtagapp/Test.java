package com.example.jesusruizcarmena.hashtagapp;

import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.jesusruizcarmena.hashtagapp.activities.MainActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Created by jesusruizcarmena on 4/1/17.
 */

@RunWith(AndroidJUnit4.class)
public class Test {
    private static final int TEST_DELAY_MILLIS = 5000;
    private static final String TEST_SEARCH_HASHTAG = "miami";

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @org.junit.Test
    public void test() throws InterruptedException {
        SystemClock.sleep(TEST_DELAY_MILLIS);
        onView(withText("#")).check(matches(isDisplayed()));
        // Input test hashtag
        ViewInteraction editText = onView(
                withId(R.id.edit_text_search_input));
        editText.perform(typeText(TEST_SEARCH_HASHTAG), closeSoftKeyboard());
        editText.perform(pressImeActionButton());
        // Check result
        SystemClock.sleep(TEST_DELAY_MILLIS);
        onView(withText("Results for #" + TEST_SEARCH_HASHTAG + ":")).check(matches(isDisplayed()));
        try {
            mainActivityTestRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivityTestRule.getActivity().onBackPressed();
                }
            });
        } catch (Throwable throwable) {
            onView(withText("#")).
                    check(matches(isDisplayed()));
        }
        // Check history
        SystemClock.sleep(TEST_DELAY_MILLIS);
        editText.perform(closeSoftKeyboard());
        SystemClock.sleep(TEST_DELAY_MILLIS);
        ViewInteraction history = onView(
                withText("#" + TEST_SEARCH_HASHTAG));
        history.perform(scrollTo());
        history.check(matches(isDisplayed()));
        history.perform(click());
        // Check result
        SystemClock.sleep(TEST_DELAY_MILLIS);
        onView(withText("Results for #" + TEST_SEARCH_HASHTAG + ":")).check(matches(isDisplayed()));
    }
}




