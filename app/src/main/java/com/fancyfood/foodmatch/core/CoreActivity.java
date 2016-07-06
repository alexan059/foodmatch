package com.fancyfood.foodmatch.core;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import com.fancyfood.foodmatch.R;

/**
 * The CoreActivity class has a custom Toolbar and Navigation Drawer to
 * be used in multiple activities without redefining them every time.
 */
public class CoreActivity extends AppCompatActivity {

    private static final String TAG = CoreActivity.class.getSimpleName();

    private DrawerLayout drawerLayout;
    private FrameLayout contentFrame;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private NavigationView navigation;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        contentFrame = (FrameLayout) findViewById(R.id.content_view);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

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

        navigation = (NavigationView) findViewById(R.id.left_drawer);
        menu = navigation.getMenu();

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_toolbar_text, R.string.close_toolbar_text);
        drawerLayout.addDrawerListener(drawerToggle);

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

    public Toolbar getToolbar() {
        return toolbar;
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public CollapsingToolbarLayout getCollapsingToolbar() {
        return collapsingToolbar;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public NavigationView getNavigation() {
        return navigation;
    }

    public Menu getMenu() {
        return menu;
    }

}
