package com.example.a402307128_smartpantrymanager;

import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Launcher screen. It only hosts the bottom navigation bar and swaps the
 * three main screens (Pantry, Suggestions, Settings) in and out of the
 * fragment container.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // if/else instead of switch: resource ids are not compile-time constants in new Android Gradle versions
            if (id == R.id.nav_pantry) {
                showFragment(new PantryFragment(), id);
                return true;
            } else if (id == R.id.nav_suggestions) {
                showFragment(new SuggestedRecipesFragment(), id);
                return true;
            } else if (id == R.id.nav_settings) {
                showFragment(new SettingsFragment(), id);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_pantry);   // first launch: show the pantry
        } else {
            updateTitle(bottomNavigation.getSelectedItemId());     // after rotation: Android restores the fragment
        }
    }

    private void showFragment(Fragment fragment, int navId) {
        updateTitle(navId);   // use the tapped item's id: the bar marks it selected only after this callback
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, fragment)
                .commit();
    }

    private void updateTitle(int navId) {
        @StringRes int title = R.string.pantry_title;
        if (navId == R.id.nav_suggestions) {
            title = R.string.suggested_recipes_title;
        } else if (navId == R.id.nav_settings) {
            title = R.string.settings_title;
        }
        setTitle(title);
    }
}
