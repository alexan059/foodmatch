package com.fancyfood.foodmatch;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.fancyfood.foodmatch.activities.MainActivity;

import org.junit.After;
import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @SmallTest
    public void testButtons() {
        onView(withId(R.id.btHungry)).perform(click());
    }

    @SmallTest
    public void testDisike() {
        onView(withId(R.id.btDislike)).perform(click());
    }

    @SmallTest
    public void testLike() {
        onView(withId(R.id.btLike)).perform(click());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
