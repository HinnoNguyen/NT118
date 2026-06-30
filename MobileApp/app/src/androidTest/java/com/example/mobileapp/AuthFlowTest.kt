package com.example.mobileapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test covering the auth flow.
 *
 * NOTE: [LoginActivity] is backed by [com.example.mobileapp.data.repository.UserRepositoryImpl],
 * which is currently an in-memory mock (see [com.example.mobileapp.domain.usecase.LoginUseCase]) —
 * it accepts any non-blank email with a password of at least 6 characters and does not require
 * a pre-existing account. On success it navigates straight to [MainActivity]. This test exercises
 * that real behavior directly with a freshly generated email, so it is self-contained and does not
 * depend on any fixture account existing in a backend.
 */
@RunWith(AndroidJUnit4::class)
class AuthFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun login_withFreshCredentials_navigatesToHomeScreen() {
        val uniqueEmail = "test+${System.currentTimeMillis()}@example.com"
        val password = "Password123"

        onView(withId(R.id.etEmail))
            .perform(clearText(), typeText(uniqueEmail), closeSoftKeyboard())
        onView(withId(R.id.etPassword))
            .perform(clearText(), typeText(password), closeSoftKeyboard())

        onView(withId(R.id.btnStartGame)).perform(click())

        // LoginViewModel.login() succeeds asynchronously and LoginActivity starts
        // MainActivity on Resource.Success. Once there, MainActivity's root view
        // (R.id.main, declared in activity_main.xml) should be displayed.
        onView(withId(R.id.main)).check(matches(isDisplayed()))
    }
}
