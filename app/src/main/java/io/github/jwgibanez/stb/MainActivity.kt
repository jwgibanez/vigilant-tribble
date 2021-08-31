package io.github.jwgibanez.stb

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import io.github.jwgibanez.stb.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.apply {
            adapter = MainPager(this@MainActivity)
            offscreenPageLimit = 1
            // Disable over-scroll animation
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    override fun onBackPressed() {
        binding.viewPager.apply {
            if (currentItem == 0) {
                super.onBackPressed()
            } else {
                currentItem = 0
            }
        }
    }
}