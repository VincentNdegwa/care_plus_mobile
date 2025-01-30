package com.example.careplus.ui.health_providers

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CaregiverPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3 // Update to the correct number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllCareProvidersFragment()
            1 -> MyDoctorsFragment()
            2 -> MyCaregiversFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
} 