package com.myattendance

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import android.widget.LinearLayout
import android.widget.ImageView


class TestInput : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ttt)

        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dotsLayout)

        val imageUrls = listOf(
            "https://res.cloudinary.com/nimg/image/upload/v1740546736/images_wrzf5y.png",
            "https://res.cloudinary.com/nimg/image/upload/v1740481628/cld-sample-4.jpg",
            "https://res.cloudinary.com/nimg/image/upload/v1740481627/samples/dessert-on-a-plate.jpg"
        )

        val adapter = ViewPagerAdapter(imageUrls)
        viewPager.adapter = adapter

        setupDots(imageUrls.size)
        autoSlideImages()
    }

    private fun setupDots(count: Int) {
        dots = arrayOfNulls(count)
        dotsLayout.removeAllViews()

        for (i in 0 until count) {
            dots[i] = ImageView(this)
            dots[i]?.setImageResource(R.drawable.dot_inactive)

            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dotsLayout.addView(dots[i], params)
        }

        dots[0]?.setImageResource(R.drawable.dot_active)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i]?.setImageResource(R.drawable.dot_inactive)
                }
                dots[position]?.setImageResource(R.drawable.dot_active)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun autoSlideImages() {
        val runnable = object : Runnable {
            override fun run() {
                val nextItem = (viewPager.currentItem + 1) % dots.size
                viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)
    }
}
