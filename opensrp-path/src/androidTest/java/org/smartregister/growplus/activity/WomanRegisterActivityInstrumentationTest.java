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
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class WomanRegisterActivityInstrumentationTest {

    @Rule
    public ActivityTestRule<org.smartregister.path.activity.LoginActivity> mActivityTestRule = new ActivityTestRule<>(org.smartregister.path.activity.LoginActivity.class);

    @Test
    public void womanRegisterActivityInstrumentationTest() {
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

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction linearLayout2 = onView(
                withId(R.id.woman_register));
        linearLayout2.perform(scrollTo(), click());

        ViewInteraction appCompatButton2 = onView(withIndex(withId(R.id.add_member),1));
        appCompatButton2.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.action_save), withText("Save"), withContentDescription("Save"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tb_top),
                                        1),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Save")));

        pressBack();

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button2), withText("Yes"),
                        withParent(allOf(withId(R.id.buttonPanel),
                                withParent(withId(R.id.parentPanel)))),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction linearLayout3 = onView(
                allOf(withId(R.id.profile_info_layout),
                        withParent(childAtPosition(
                                withId(R.id.list),
                                2)),
                        isDisplayed()));
        linearLayout3.perform(click());

        ViewInteraction customFontTextView = onView(
                allOf(withId(R.id.record_all_tv), withText("Record all"), isDisplayed()));
        customFontTextView.perform(click());

        ViewInteraction textView3 = onView(anyOf(withId(R.id.name), withText("Bushra .")));
        textView3.check(matches(withText("Bushra .")));

        ViewInteraction button = onView(
                allOf(withId(R.id.vaccinate_today),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                        0),
                                5),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.vaccinate_earlier),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                        0),
                                6),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                anyOf(withId(R.id.cancel)));
        button3.check(matches(isDisplayed()));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.vaccinate_earlier), withText("Vaccination done earlier"), isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction appCompatButton5 = onView(
                anyOf(withId(R.id.cancel)));
        appCompatButton5.perform(click());

        ViewInteraction vaccineCard = onView(
                withIndex(childAtPosition(
                        withId(R.id.vaccines_gv),
                        0),0));
        vaccineCard.perform(click());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.vaccinate_today), withText("Vaccination done today"), isDisplayed()));
        appCompatButton6.perform(click());

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.undo_b), withText("Undo"), isDisplayed()));
        appCompatButton7.perform(click());

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.yes_undo), withText("Yes, undo vaccination"), isDisplayed()));
        appCompatButton8.perform(click());

        pressBack();

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.edt_search), isDisplayed()));
        appCompatEditText3.perform(replaceText("bushra"), closeSoftKeyboard());

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.btn_search_cancel), isDisplayed()));
        imageButton.perform(click());

        ViewInteraction linearLayout4 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout4.perform(click());

        ViewInteraction appCompatButton9 = onView(
                allOf(withId(R.id.logout_b), withText("Log out"), isDisplayed()));
        appCompatButton9.perform(click());

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
