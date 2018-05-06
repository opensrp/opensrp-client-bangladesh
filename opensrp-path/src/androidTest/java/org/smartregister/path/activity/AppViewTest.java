package org.smartregister.path.activity;


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
import org.smartregister.path.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AppViewTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void appViewTest() {
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

        ViewInteraction linearLayout3 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout3.perform(click());

        ViewInteraction linearLayout4 = onView(
                withId(R.id.child_register));
        linearLayout4.perform(scrollTo(), click());

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.global_search),
                        withParent(allOf(withId(R.id.register_nav_bar_container),
                                withParent(withId(R.id.toolbar)))),
                        isDisplayed()));
        imageButton.perform(click());

        ViewInteraction materialEditText = onView(
                allOf(withId(R.id.mother_guardian_name), isDisplayed()));
        materialEditText.perform(replaceText("bg"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.search), withText("Search"),
                        withParent(allOf(withId(R.id.adv_search_layout),
                                withParent(withId(R.id.advanced_search_form)))),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction linearLayout5 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout5.perform(click());

        ViewInteraction materialEditText2 = onView(
                allOf(withId(R.id.mother_guardian_name), withText("bg"), isDisplayed()));
//        materialEditText2.perform(replaceText(""), closeSoftKeyboard());

        ViewInteraction materialEditText3 = onView(
                allOf(withId(R.id.first_name), isDisplayed()));
//        materialEditText3.perform(replaceText("rahim"), closeSoftKeyboard());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.search), withText("Search"),
                        withParent(allOf(withId(R.id.adv_search_layout),
                                withParent(withId(R.id.advanced_search_form)))),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction linearLayout6 = onView(
                allOf(withId(R.id.child_profile_info_layout),
                        withParent(childAtPosition(
                                withId(R.id.list),
                                1)),
                        isDisplayed()));
        linearLayout6.perform(click());

        ViewInteraction imageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.location_switching_toolbar)),
                        isDisplayed()));
        imageButton2.perform(click());

        ViewInteraction linearLayout7 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout7.perform(click());

        ViewInteraction linearLayout8 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout8.perform(click());

        ViewInteraction linearLayout9 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout9.perform(click());

        ViewInteraction linearLayout10 = onView(
                withId(R.id.household_register));
        linearLayout10.perform(scrollTo(), click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.add_member), withText("add \n member"), isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.add_child), withText("add child"), isDisplayed()));
        appCompatButton5.perform(click());

        ViewInteraction materialEditText4 = onView(
                withClassName(is("com.rengwuxian.materialedittext.MaterialEditText")));
        materialEditText4.perform(scrollTo(), replaceText("m"), closeSoftKeyboard());

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.snackbar_text), withText("2 mother/guardian match(es)."), isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.list_view),
                                withParent(withId(R.id.custom))),
                        1),
                        isDisplayed()));
        relativeLayout.perform(click());

        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(R.id.snackbar_action), withText("Clear"), isDisplayed()));
        appCompatTextView3.perform(click());

        pressBack();

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button2), withText("Yes"),
                        withParent(allOf(withId(R.id.buttonPanel),
                                withParent(withId(R.id.parentPanel)))),
                        isDisplayed()));
        appCompatButton6.perform(click());

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton7.perform(click());

        ViewInteraction linearLayout11 = onView(
                allOf(withId(R.id.child_profile_info_layout),
                        withParent(childAtPosition(
                                withId(R.id.list),
                                1)),
                        isDisplayed()));
        linearLayout11.perform(click());

        ViewInteraction linearLayout12 = onView(
                allOf(withId(R.id.profile_name_layout), isDisplayed()));
        linearLayout12.perform(click());

        ViewInteraction linearLayout13 = onView(
                allOf(withId(R.id.profile_name_layout), isDisplayed()));
        linearLayout13.perform(click());

        ViewInteraction linearLayout14 = onView(
                allOf(withId(R.id.profile_name_layout), isDisplayed()));
        linearLayout14.perform(click());

        ViewInteraction linearLayout15 = onView(
                allOf(withId(R.id.profile_name_layout), isDisplayed()));
        linearLayout15.perform(click());

        pressBack();

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
}
