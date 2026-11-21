package com.example.expiretrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.expiretrack.databinding.ActivityMainBinding
import com.example.expiretrack.notification.NotificationHelper
import com.example.expiretrack.ui.activity.AddProductActivity
import com.example.expiretrack.ui.fragment.DashboardFragment
import com.example.expiretrack.ui.fragment.NotificationFragment
import com.example.expiretrack.ui.fragment.ProductFragment
import com.example.expiretrack.ui.fragment.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var dashboardFragment = DashboardFragment()
    private var productFragment = ProductFragment()
    private var notificationFragment = NotificationFragment()
    private var profileFragment = ProfileFragment()

    private var activeFragment: Fragment = dashboardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createNotificationChannel(this)

        setupFragmentContainer(savedInstanceState)
        setupBottomNavigation()

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }
    }

    private fun setupFragmentContainer(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, profileFragment, "4").hide(profileFragment)
                .add(R.id.fragment_container, notificationFragment, "3").hide(notificationFragment)
                .add(R.id.fragment_container, productFragment, "2").hide(productFragment)
                .add(R.id.fragment_container, dashboardFragment, "1")
                .commit()
            activeFragment = dashboardFragment
        } else {
            dashboardFragment = (supportFragmentManager.findFragmentByTag("1") as? DashboardFragment) ?: DashboardFragment()
            productFragment = (supportFragmentManager.findFragmentByTag("2") as? ProductFragment) ?: ProductFragment()
            notificationFragment = (supportFragmentManager.findFragmentByTag("3") as? NotificationFragment) ?: NotificationFragment()
            profileFragment = (supportFragmentManager.findFragmentByTag("4") as? ProfileFragment) ?: ProfileFragment()

            activeFragment = when (binding.bottomNavigation.selectedItemId) {
                R.id.nav_products -> productFragment
                R.id.nav_notifications -> notificationFragment
                R.id.nav_profile -> profileFragment
                else -> dashboardFragment
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    switchFragment(dashboardFragment)
                    binding.fabAddProduct.visibility = View.VISIBLE
                    true
                }
                R.id.nav_products -> {
                    switchFragment(productFragment)
                    binding.fabAddProduct.visibility = View.VISIBLE
                    true
                }
                R.id.nav_notifications -> {
                    switchFragment(notificationFragment)
                    binding.fabAddProduct.visibility = View.GONE
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(profileFragment)
                    binding.fabAddProduct.visibility = View.GONE
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (activeFragment != targetFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(targetFragment)
                .commit()
            activeFragment = targetFragment
        }
    }

    fun navigateToProductsTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_products
    }
}