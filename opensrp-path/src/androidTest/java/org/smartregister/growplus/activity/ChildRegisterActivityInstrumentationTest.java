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
import org.smartregister.path.activity.*;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ChildRegisterActivityInstrumentationTest {

    @Rule
    public ActivityTestRule<org.smartregister.path.activity.LoginActivity> mActivityTestRule = new ActivityTestRule<>(org.smartregister.path.activity.LoginActivity.class);

    @Test
    public void childRegisterActivityInstrumentationTest() {
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
                withId(R.id.child_register));
        linearLayout2.perform(scrollTo(), click());

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.global_search),
                        withParent(allOf(withId(R.id.register_nav_bar_container),
                                withParent(withId(R.id.toolbar)))),
                        isDisplayed()));
        imageButton.perform(click());

        ViewInteraction materialEditText = onView(
                allOf(withId(R.id.first_name), isDisplayed()));
        materialEditText.perform(replaceText("rahim"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.search), withText("Search"),
                        withParent(allOf(withId(R.id.adv_search_layout),
                                withParent(withId(R.id.advanced_search_form)))),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction linearLayout3 = onView(withIndex(withId(R.id.move_to_catchment),0));
        linearLayout3.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("No"),
                        withParent(allOf(withId(R.id.buttonPanel),
                                withParent(withId(R.id.parentPanel)))),
                        isDisplayed()));
        appCompatButton3.perform(click());

        pressBack();

        pressBack();

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button2), withText("Yes"),
                        withParent(allOf(withId(R.id.buttonPanel),
                                withParent(withId(R.id.parentPanel)))),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction linearLayoutagain = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction linearLayout2again = onView(
                withId(R.id.child_register));
        linearLayout2.perform(scrollTo(), click());


        ViewInteraction linearLayout4 = onView(withIndex(withId(R.id.record_vaccination), 0));
        linearLayout4.perform(click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton5.perform(click());

        pressBack();

        ViewInteraction linearLayout5 = onView(withIndex(withId(R.id.record_weight),0));
        linearLayout5.perform(click());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.weight_taken_earlier), withText("Weight taken earlier"), isDisplayed()));
        appCompatButton6.perform(click());

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton7.perform(click());

        ViewInteraction customFontTextView = onView(
                allOf(withId(R.id.record_all_tv), withText("Record all"), isDisplayed()));
        customFontTextView.perform(click());

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton8.perform(click());

        ViewInteraction vaccineCard = onView(
                withIndex(childAtPosition(
                        withId(R.id.vaccines_gv),
                        0),0));
        vaccineCard.perform(click());

        ViewInteraction appCompatButton9 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton9.perform(click());

        ViewInteraction vaccineCard2 = onView(
                withIndex(childAtPosition(
                        withId(R.id.vaccines_gv),
                        0),0));
        vaccineCard2.perform(click());

        ViewInteraction appCompatButton10 = onView(
                allOf(withId(R.id.set), withText("Set"), isDisplayed()));
        appCompatButton10.perform(click());

        ViewInteraction appCompatButton11 = onView(
                allOf(withId(R.id.undo_b), withText("Undo"), isDisplayed()));
        appCompatButton11.perform(click());

        ViewInteraction appCompatButton12 = onView(
                allOf(withId(R.id.yes_undo), withText("Yes, undo vaccination"), isDisplayed()));
        appCompatButton12.perform(click());

        pressBack();

        ViewInteraction linearLayout6 = onView(
                allOf(withId(R.id.child_profile_info_layout),
                        withParent(childAtPosition(
                                withId(R.id.list),
                                1)),
                        isDisplayed()));
        linearLayout6.perform(click());

        ViewInteraction linearLayout7 = onView(
                withId(R.id.profile_name_layout));
        linearLayout7.perform(scrollTo(), click());


        ViewInteraction appCompatTextView = onView(
                allOf(withText("Under Five History"), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatTextView2 = onView(
                allOf(withText("Registration Data"), isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction linearLayout8 = onView(
                allOf(withId(R.id.statusview), isDisplayed()));
        linearLayout8.perform(click());

        pressBack();

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(R.id.title), withText("Record BCG 2 (if no scar)"), isDisplayed()));
        appCompatTextView3.perform(click());

        //////////////////////////////////////////////////////////////////////////////
        ViewInteraction appCompatTextViewunderfivehistory = onView(
                allOf(withText("Under Five History"), isDisplayed()));
        appCompatTextViewunderfivehistory.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView_2 = onView(
                allOf(withId(R.id.title), withText("Edit immunisation data"), isDisplayed()));
        appCompatTextView_2.perform(click());

        ViewInteraction appCompatButton_2 = onView(
                withIndex(withId(R.id.undo_b),1));
        appCompatButton_2.perform(click());

        ViewInteraction appCompatButton_3 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton_3.perform(click());

        ViewInteraction imageButtonup = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.child_detail_toolbar)),
                        isDisplayed()));
        imageButtonup.perform(click());

        ViewInteraction imageButton_2 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.location_switching_toolbar)),
                        isDisplayed()));
        imageButton_2.perform(click());

        ViewInteraction linearLayout_5 = onView(withIndex(withId(R.id.child_profile_info_layout),1));
        linearLayout_5.perform(click());

        ViewInteraction linearLayout_6 = onView(
                withId(R.id.profile_name_layout));
        linearLayout_6.perform(scrollTo(), click());

        ViewInteraction appCompatTextView_3 = onView(
                allOf(withText("Under Five History"), isDisplayed()));
        appCompatTextView_3.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView_4 = onView(
                allOf(withId(R.id.title), withText("Edit weight data"), isDisplayed()));
        appCompatTextView_4.perform(click());

        ViewInteraction appCompatButton_4 = onView(
                allOf(withId(R.id.edit), withText("Edit"), isDisplayed()));
        appCompatButton_4.perform(click());

        ViewInteraction appCompatButton_5 = onView(
                allOf(withId(R.id.cancel), withText("Cancel"), isDisplayed()));
        appCompatButton_5.perform(click());

        ViewInteraction customFontTextViewsave = onView(
                allOf(withId(R.id.save), withText("SAVE"), isDisplayed()));
        customFontTextViewsave.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView_5 = onView(
                allOf(withId(R.id.title), withText("Edit registration data"), isDisplayed()));
        appCompatTextView_5.perform(click());

        pressBack();

        ViewInteraction appCompatButton_6 = onView(
                allOf(withId(android.R.id.button2), withText("Yes"),
                        withParent(allOf(withId(R.id.buttonPanel),
                                withParent(withId(R.id.parentPanel)))),
                        isDisplayed()));
        appCompatButton_6.perform(click());

//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        pressBack();

        ViewInteraction imageButton3 = onView(
                allOf(withId(R.id.growth_chart_button), isDisplayed()));
        imageButton3.perform(click());

        ViewInteraction imageButton4 = onView(
                withId(R.id.scroll_button));
        imageButton4.perform(scrollTo(), click());

        ViewInteraction imageButton5 = onView(
                withId(R.id.scroll_button));
        imageButton5.perform(scrollTo(), click());

        ViewInteraction appCompatButton_7 = onView(
                allOf(withId(R.id.done), withText("Done"), isDisplayed()));
        appCompatButton_7.perform(click());

        ///////////////////////////////////////////////////////////////////////////////////
        pressBack();

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.edt_search), isDisplayed()));
        appCompatEditText3.perform(replaceText("Tahsan"), closeSoftKeyboard());

        ViewInteraction imageButton2 = onView(
                allOf(withId(R.id.btn_search_cancel), isDisplayed()));
        imageButton2.perform(click());

        ViewInteraction linearLayout9 = onView(
                allOf(withId(R.id.btn_back_to_home),
                        withParent(allOf(withId(R.id.back_btn_layout),
                                withParent(withId(R.id.register_nav_bar_container)))),
                        isDisplayed()));
        linearLayout9.perform(click());

        ViewInteraction appCompatButton15 = onView(
                allOf(withId(R.id.logout_b), withText("Log out"), isDisplayed()));
        appCompatButton15.perform(click());

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
