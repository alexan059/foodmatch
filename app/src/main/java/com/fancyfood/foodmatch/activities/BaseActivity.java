package com.fancyfood.foodmatch.activities;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.fancyfood.foodmatch.R;

/**
 * The BaseActivity class has a custom Toolbar and Navigation Drawer to
 * be used in multiple activities without redefining them every time.
 */
public class BaseActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FrameLayout contentFrame;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        contentFrame = (FrameLayout) findViewById(R.id.content_view);

        initToolbarAndMenu();
    }

    /**
     * Initialize Toolbar and Navigation Drawer.
     */
    private void initToolbarAndMenu() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_toolbar_text, R.string.close_toolbar_text);
        drawerLayout.addDrawerListener(drawerToggle);

        String[] navigationItems = {"Option A", "Option B"};
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navigationItems));

    }

    /**
     * Takes the view from child and inflates into the base layout.
     *
     * @param layoutId
     */
    protected void inflateView(int layoutId) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = layoutInflater.inflate(layoutId, null, false);
        contentFrame.addView(contentView, 0);
    }
}
