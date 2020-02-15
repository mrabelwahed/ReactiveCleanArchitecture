package com.gify.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gify.R
import com.gify.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import viewassertion.RecyclerViewItemCountAssertion

@RunWith(AndroidJUnit4::class)
class GifListFragmentTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingResource)
    }

    @Test
    fun onLaunchActivityGifListIsDisplayed() {
        onView(withId(R.id.gifList))
            .check(ViewAssertions.matches(isDisplayed()))
    }


    @Test
    fun shouldDisplayGifDetailsWhenGifListItemClicked() {
        onView(withId(R.id.gifList))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<GifyListAdapter.GifViewHolder>(
                    10,
                    click()
                )
            )
        onView(withId(R.id.detailsView))
            .check(ViewAssertions.matches(isDisplayed()))
    }


    @Test

    fun should_gif_list_contains_20_gifs_at_first_page(){
        onView(withId(R.id.gifList))
            .check(RecyclerViewItemCountAssertion.hasItemCount(20))
    }



}