package io.github.jwgibanez.stb

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.jwgibanez.stb.ui.main.MainFragment
import io.github.jwgibanez.stb.ui.scan.BarcodeScanFragment

class MainPager(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainFragment.newInstance()
            else -> BarcodeScanFragment.newInstance()
        }
    }
}