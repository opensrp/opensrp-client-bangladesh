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
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.growplus.R;
import org.smartregister.path.activity.*;

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
import static org.hamcrest.Matchers.anyOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HouseholdAddMemberFragmentInstrumentationTest {

    @Rule
    public ActivityTestRule<org.smartregister.path.activity.LoginActivity> mActivityTestRule = new ActivityTestRule<>(org.smartregister.path.activity.LoginActivity.class);

    @Test
    public void householdAddMemberFragmentInstrumentationTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.login_userNameText),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatEditText.perform(scrollTo(), replaceText("raihan"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.login_passwordText),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatEditText2.perform(scrollTo(), replaceText("Raihan@123"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.login_loginButton), withText("Log In"),
                        withParent(allOf(withId(R.id.credentialsCanvasLL),
                                withParent(withId(R.id.canvasRL))))));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction listView = onView(
                allOf(withId(R.id.list),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                        3),
                                0),
                        isDisplayed()));
//
//        ViewInteraction button = onView(
//                anyOf(withId(R.id.add_member),
//                        childAtPosition(
//                                childAtPosition(
//                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
//                                        4),
//                                0),
//                        isDisplayed()));
//        button.check(matches(isDisplayed()));
//
//        ViewInteraction button2 = onView(
//                allOf(withId(R.id.add_member),
//                        childAtPosition(
//                                childAtPosition(
//                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
//                                        4),
//                                0),
//                        isDisplayed()));
//        button2.check(matches(isDisplayed()));

        ViewInteraction appCompatButton2 = onView(withIndex(withId(R.id.add_member), 1));
        appCompatButton2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.add_woman),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                        0),
                                4),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction button4 = onView(
                allOf(withId(R.id.add_child),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                        0),
                                5),
                        isDisplayed()));
        button4.check(matches(isDisplayed()));

        ViewInteraction button5 = onView(
                allOf(withId(R.id.cancel),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                        6),
                                0),
                        isDisplayed()));
        button5.check(matches(isDisplayed()));

        pressBack();

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.logout_b), withText("Log out"), isDisplayed()));
        appCompatButton3.perform(click());

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
