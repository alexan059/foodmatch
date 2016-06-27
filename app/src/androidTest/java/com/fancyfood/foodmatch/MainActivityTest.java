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

    // Button IDs
    private static final int BT_HUNGRY = R.id.btHungry;
    private static final int BT_LIKE = R.id.btLike;
    private static final int BT_DISLIKE = R.id.btDislike;

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
        // Start fling
        onView(withId(BT_HUNGRY)).perform(click());

        // 3 x dislike
        onView(withId(BT_DISLIKE)).perform(click());
        onView(withId(BT_DISLIKE)).perform(click());
        onView(withId(BT_DISLIKE)).perform(click());

        // Wait for maps
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 100 x dislike
        for (int i = 0; i < 5; i++) {
            onView(withId(BT_DISLIKE)).perform(click());
        }

        // 1 x like
        onView(withId(BT_LIKE)).perform(click());

        // Wait for maps
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
