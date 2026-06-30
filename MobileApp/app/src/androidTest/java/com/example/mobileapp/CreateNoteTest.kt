package com.example.mobileapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test covering note creation.
 *
 * NOTE: [NotesActivity] talks directly to Firebase Auth + Firestore
 * (see [com.example.mobileapp.data.repository.FirestoreNoteRepositoryImpl]) — it reads
 * `FirebaseAuth.getInstance().currentUser` and is a no-op if that is null. The app's own
 * Login/Register screens never call real Firebase Auth (they use an in-memory mock
 * repository, see [com.example.mobileapp.data.repository.UserRepositoryImpl]), so there is
 * currently no UI path that leaves a real FirebaseUser signed in. To exercise NotesActivity
 * in a self-contained way, this test signs in to real Firebase Auth directly with a freshly
 * generated unique account before launching the activity.
 */
@RunWith(AndroidJUnit4::class)
class CreateNoteTest {

    private lateinit var auth: FirebaseAuth

    @Before
    fun signInWithFreshFirebaseAccount() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Ensure Firebase is initialized in the instrumentation process.
        FirebaseApp.initializeApp(context)
        auth = FirebaseAuth.getInstance()

        val uniqueEmail = "test+${System.currentTimeMillis()}@example.com"
        val password = "Password123"

        runBlocking {
            auth.createUserWithEmailAndPassword(uniqueEmail, password).await()
        }
    }

    @After
    fun signOut() {
        auth.currentUser?.delete()
        auth.signOut()
    }

    @Test
    fun createNote_appearsInNotesList() {
        val scenario = ActivityScenario.launch(NotesActivity::class.java)

        val uniqueTitle = "Test Note ${System.currentTimeMillis()}"
        val content = "This is a note created by an instrumented test."

        onView(withId(R.id.btnNewNote)).perform(click())

        onView(withId(R.id.etNoteTitle)).perform(typeText(uniqueTitle), closeSoftKeyboard())
        onView(withId(R.id.etNoteContent)).perform(typeText(content), closeSoftKeyboard())

        onView(withId(R.id.btnSaveNote)).perform(click())

        // NotesViewModel.saveNote() persists to Firestore then reloads the list,
        // re-rendering rows into notesListContainer (NotesActivity.renderNotes()).
        onView(withId(R.id.notesListContainer))
            .check(matches(hasDescendant(withText(uniqueTitle))))

        scenario.close()
    }
}
