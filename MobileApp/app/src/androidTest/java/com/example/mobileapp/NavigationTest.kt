package com.example.mobileapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test covering navigation between screens.
 *
 * NOTE: the bottom-nav row in activity_main.xml has no view IDs (navHome/navQuest/etc. are
 * only declared in activity_story.xml and activity_timer.xml), so MainActivity's bottom nav
 * is not currently wired to anything — NavHelper.setup() is only invoked from NotesActivity
 * and StoryActivity, neither of which is reachable through MainActivity's bottom nav either.
 * Additionally StoryActivity, TimerActivity are not declared in AndroidManifest.xml, so they
 * cannot be launched/navigated to at all without crashing.
 *
 * The one navigation path in the app that is fully wired end-to-end — real click listener,
 * matching view IDs, and both activities declared in the manifest — is
 * MainActivity.userInfoCard -> ProfileActivity (see MainActivity.setupUI()) and
 * ProfileActivity.btnBack -> finish() back to MainActivity. This test exercises that real,
 * working navigation flow.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun tappingUserInfoCard_navigatesToProfileScreen() {
        onView(withId(R.id.main)).check(matches(isDisplayed()))

        onView(withId(R.id.userInfoCard)).perform(click())

        // ProfileActivity reuses the same root view id (R.id.main) per activity_profile.xml.
        onView(withId(R.id.main)).check(matches(isDisplayed()))
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()))
    }

    @Test
    fun tappingBackOnProfileScreen_returnsToHomeScreen() {
        onView(withId(R.id.userInfoCard)).perform(click())
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()))

        onView(withId(R.id.btnBack)).perform(click())

        // ProfileActivity.btnBack just calls finish(), dropping back to MainActivity.
        onView(withId(R.id.main)).check(matches(isDisplayed()))
        onView(withId(R.id.userInfoCard)).check(matches(isDisplayed()))
    }
}
