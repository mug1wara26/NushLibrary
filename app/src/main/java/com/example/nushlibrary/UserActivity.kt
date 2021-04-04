package com.example.nushlibrary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.nushlibrary.adminFragments.AdminHomeFragment
import com.example.nushlibrary.booksFragment.BooksFragment
import com.example.nushlibrary.userFragments.UserHomeFragment
import com.example.nushlibrary.userFragments.UserSettingsFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class UserActivity: AppCompatActivity() {
    lateinit var drawer: DrawerLayout

    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Set a Toolbar to replace the ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // This will display an Up icon (<-), we will replace it with the correct icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Replacing it with menu icon
        toolbar.setNavigationIcon(R.drawable.drawer_icon)

        // Find our drawer view
        drawer = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nvView)
        // Setup drawer view
        setupDrawerContent(navigationView)
        // Set default menu item to be Home
        selectDrawerItem(navigationView.menu[0])
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        when (item.itemId) {
            android.R.id.home -> {
                drawer.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun setupDrawerContent(navigationView: NavigationView) {
        val headerView = navigationView.getHeaderView(0)
        // Set header to show user display name
        headerView.findViewById<TextView>(R.id.nav_view_display_name).text = "Hi, ${mainUser.displayName}"

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logOut) {
                // Sign out user
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
            else selectDrawerItem(menuItem)
            // I didn't know you could just return like this woah
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        // placeholder
        val fragment: Fragment

        // Matches menu item id with the corresponding fragment class, default will be home fragment
        val fragmentClass: Class<*> = when(menuItem.itemId) {
            R.id.home -> UserHomeFragment::class.java
            R.id.books -> BooksFragment::class.java
            R.id.settings -> UserSettingsFragment::class.java
            else -> AdminHomeFragment::class.java
        }

        try {
            fragment = fragmentClass.newInstance() as Fragment

            // Insert the fragment by replacing any existing fragment
            val fragmentManager: FragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()

            // Highlight currently selected menu item
            menuItem.isChecked = true
            // Set action bar title
            title = menuItem.title
            // Close the navigation drawer
            drawer.closeDrawers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}