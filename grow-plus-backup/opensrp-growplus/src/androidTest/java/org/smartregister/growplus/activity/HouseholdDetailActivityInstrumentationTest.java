package org.smartregister.growplus.activity;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.growplus.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HouseholdDetailActivityInstrumentationTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void householdDetailActivityInstrumentationTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.login_userNameText),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.login_userNameText),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatEditText2.perform(scrollTo(), replaceText("raihan"), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.login_passwordText),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatEditText3.perform(scrollTo(), replaceText("Raihan@123"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.login_loginButton), withText("Log In"),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.edt_search), isDisplayed()));
        appCompatEditText4.perform(replaceText("Tanzim"), closeSoftKeyboard());

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.btn_search_cancel), isDisplayed()));
        imageButton.perform(click());

        ViewInteraction linearLayout = onView(withIndex(withId(R.id.child_profile_info_layout),1));
        linearLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.title), withText("Household Details"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.location_switching_toolbar),
                                        1),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Household Details")));

        pressBack();

        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout2.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.logout_b), withText("Log out"), isDisplayed()));
        appCompatButton2.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
