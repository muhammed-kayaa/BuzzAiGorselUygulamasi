package com.example.buzzai

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load HomeFragment as default on app launch
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Setup BottomNavigationView listener
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_studio -> StudioFragment()
                R.id.nav_create -> UretFragment() // Bak, burayı 'nav_create' yaptık!
                R.id.nav_templates -> TemplatesFragment()
                R.id.nav_profile -> ProfileFragment() // Senin menünde profil de var, onu da ekledik
                else -> HomeFragment() // Hiçbiri değilse ana sayfaya dön
            }
            loadFragment(fragment)
            true
        }
    }

    /**
     * Helper function to replace fragments in the fragment container
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}