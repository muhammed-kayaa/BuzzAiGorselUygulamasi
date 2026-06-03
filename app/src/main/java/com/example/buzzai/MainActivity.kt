package com.example.buzzai

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private var selectedNavItemId: Int = R.id.custom_nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupCustomNavigation()

        if (savedInstanceState == null) {
            updateNavigationUI(selectedNavItemId)
            loadFragment(HomeFragment())
        }
    }

    private fun setupCustomNavigation() {
        findViewById<LinearLayout>(R.id.custom_nav_home).setOnClickListener { onNavItemClicked(it.id, HomeFragment()) }
        findViewById<LinearLayout>(R.id.custom_nav_store).setOnClickListener { onNavItemClicked(it.id, StoreFragment()) }
        findViewById<LinearLayout>(R.id.custom_nav_create).setOnClickListener { onNavItemClicked(it.id, StudioFragment()) }
        // BURASI DEĞİŞTİ: Artık HistoryFragment'ı açıyor
        findViewById<LinearLayout>(R.id.custom_nav_history).setOnClickListener { onNavItemClicked(it.id, HistoryFragment()) }
        findViewById<LinearLayout>(R.id.custom_nav_profile).setOnClickListener { onNavItemClicked(it.id, ProfileFragment()) }
    }

    private fun onNavItemClicked(clickedViewId: Int, fragment: Fragment) {
        if (selectedNavItemId == clickedViewId) return

        selectedNavItemId = clickedViewId
        updateNavigationUI(selectedNavItemId)
        loadFragment(fragment)
    }

    private fun updateNavigationUI(selectedId: Int) {
        val activeColor = Color.parseColor("#FFFFFF")
        val inactiveColor = Color.parseColor("#7A7A8C")
        val createIconColor = Color.parseColor("#FFFFFF")

        resetAllNavItems(inactiveColor, createIconColor)

        when (selectedId) {
            R.id.custom_nav_home -> highlightItem(R.id.custom_nav_home, R.id.iv_nav_home, R.id.tv_nav_home, activeColor)
            R.id.custom_nav_store -> highlightItem(R.id.custom_nav_store, R.id.iv_nav_store, R.id.tv_nav_store, activeColor)
            R.id.custom_nav_create -> {
                findViewById<ImageView>(R.id.iv_nav_create).setColorFilter(activeColor, PorterDuff.Mode.SRC_IN)
            }
            R.id.custom_nav_history -> highlightItem(R.id.custom_nav_history, R.id.iv_nav_history, R.id.tv_nav_history, activeColor)
            R.id.custom_nav_profile -> highlightItem(R.id.custom_nav_profile, R.id.iv_nav_profile, R.id.tv_nav_profile, activeColor)
        }
    }

    private fun resetAllNavItems(inactiveColor: Int, createIconColor: Int) {
        val layouts = listOf(R.id.custom_nav_home, R.id.custom_nav_store, R.id.custom_nav_history, R.id.custom_nav_profile)
        val icons = listOf(R.id.iv_nav_home, R.id.iv_nav_store, R.id.iv_nav_history, R.id.iv_nav_profile)
        val texts = listOf(R.id.tv_nav_home, R.id.tv_nav_store, R.id.tv_nav_history, R.id.tv_nav_profile)

        for (i in icons.indices) {
            findViewById<LinearLayout>(layouts[i]).background = null
            findViewById<ImageView>(icons[i]).setColorFilter(inactiveColor, PorterDuff.Mode.SRC_IN)
            findViewById<TextView>(texts[i]).setTextColor(inactiveColor)
        }

        findViewById<LinearLayout>(R.id.custom_nav_create).background = null
        findViewById<ImageView>(R.id.iv_nav_create).setColorFilter(createIconColor, PorterDuff.Mode.SRC_IN)
    }

    private fun highlightItem(layoutId: Int, ivId: Int, tvId: Int, color: Int) {
        findViewById<LinearLayout>(layoutId).background = ContextCompat.getDrawable(this, R.drawable.nav_item_selected_bg)
        findViewById<ImageView>(ivId).setColorFilter(color, PorterDuff.Mode.SRC_IN)
        findViewById<TextView>(tvId).setTextColor(color)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun simulateNavigationClick(customNavViewId: Int) {
        findViewById<LinearLayout>(customNavViewId)?.performClick()
    }
}